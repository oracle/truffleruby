class PSD
  # Parses the full preview image at the end of the PSD document.
  class Image
    include ImageFormat::RAW
    include ImageFormat::RLE
    include ImageMode::CMYK
    include ImageMode::Greyscale
    include ImageMode::RGB
    include Export::PNG

    attr_reader :pixel_data, :opacity, :has_mask
    alias :has_mask? :has_mask

    # All of the possible compression formats Photoshop uses.
    COMPRESSIONS = [
      'Raw',
      'RLE',
      'ZIP',
      'ZIPPrediction'
    ].freeze

    # Store a reference to the file and the header. We also do a few simple calculations
    # to figure out the number of pixels in the image and the length of each channel.
    def initialize(file, header)
      @file = file
      @header = header

      @num_pixels = width * height
      @num_pixels *= 2 if depth == 16

      calculate_length
      @channel_data = []
      @pixel_data = []
      @opacity = 1.0
      @has_mask = false

      @start_pos = @file.tell
      @end_pos = @start_pos + @length

      PSD.logger.debug "Image: #{width}x#{height}, length = #{@length}, mode = #{@header.mode_name}, position = #{@start_pos}"

      # Each color channel is represented by a unique ID
      @channels_info = [
        {id: 0},
        {id: 1},
        {id: 2}
      ]

      @channels_info << {id: -1} if channels == 4
    end

    # Begins parsing the image by first figuring out the compression format used, and then
    # by reading the image data.
    def parse
      @compression = parse_compression!

      # ZIP not implemented
      if [2, 3].include?(@compression)
        PSD.logger.debug "Warning: ZIP image compression not supported yet. Skipping."
        @file.seek @end_pos and return
      end

      PSD.logger.debug "Compression: id = #{@compression}, name = #{COMPRESSIONS[@compression]}"

      parse_image_data!

      return self
    end

    # We delegate a few useful methods to the header.
    [:height, :width, :channels, :depth, :mode].each do |attribute|
      define_method attribute do
        @header.send(attribute)
      end
    end

    private

    def calculate_length
      @length = case depth
      when 1 then (width + 7) / 8 * height
      when 16 then width * height * 2
      else width * height
      end

      @channel_length = @length
      @length *= channels
    end

    def parse_compression!
      @file.read_short
    end

    def parse_image_data!
      case @compression
      when 0 then parse_raw!
      when 1 then parse_rle!
      when 2, 3 then parse_zip!
      else @file.seek(@end_pos)
      end

      process_image_data
    end

    def process_image_data
      case mode
      when 1 then combine_greyscale_channel
      when 3 then combine_rgb_channel
      when 4 then combine_cmyk_channel
      end

      @channel_data = nil
    end

    def pixel_step
      depth == 8 ? 1 : 2
    end

    def pixel(i)
      @pixel_data[i]
    end
  end
end