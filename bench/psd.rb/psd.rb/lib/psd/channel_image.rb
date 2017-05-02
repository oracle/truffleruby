require_relative 'image'

class PSD
  # Represents an image for a single layer, which differs slightly in format from
  # the full size preview image.
  class ChannelImage < Image
    include ImageFormat::LayerRLE
    include ImageFormat::LayerRAW

    attr_reader :width, :height, :mask_data
    
    def initialize(file, header, layer)
      @layer = layer

      @width = @layer.width
      @height = @layer.height

      super(file, header)

      @channels_info = @layer.channels_info
      @has_mask = @layer.mask.width * @layer.mask.height > 0
      @opacity = @layer.opacity / 255.0
      @mask_data = []
    end

    def skip
      PSD.logger.debug "Skipping channel image data. Layer = #{@layer.name}"
      @channels_info.each do |ch|
        @file.seek ch[:length], IO::SEEK_CUR
      end
    end

    def channels
      @layer.channels
    end

    def parse
      PSD.logger.debug "Layer = #{@layer.name}, Size = #{width}x#{height}"
      PSD.logger.debug @channels_info

      @chan_pos = 0
      @channels_info.each do |ch_info|
        @ch_info = ch_info
        if ch_info[:length] <= 0
          parse_compression! and next
        end

        # If the ID of this current channel is -2, then we assume the dimensions
        # of the layer mask.
        if ch_info[:id] == -2
          @width = @layer.mask.width
          @height = @layer.mask.height
        else
          @width = @layer.width
          @height = @layer.height
        end

        @length = @width * @height

        start = @file.tell

        PSD.logger.debug "Channel ##{ch_info[:id]}, length = #{ch_info[:length]}"
        parse_image_data!

        finish = @file.tell

        if finish != start + ch_info[:length]
          PSD.logger.error "Read incorrect number of bytes for channel ##{ch_info[:id]}. Expected = #{ch_info[:length]}, Actual = #{finish - start}"
          @file.seek start + ch_info[:length]
        end
      end

      if @channel_data.length != (@length * @channels_info.length)
        PSD.logger.error "#{@channel_data.length} read; expected #{@length}"
      end

      @width = @layer.width
      @height = @layer.height

      parse_user_mask
      process_image_data
    end

    def parse_image_data!
      @compression = parse_compression!

      case @compression
      when 0 then parse_raw!
      when 1 then parse_rle!
      when 2, 3 then parse_zip!
      else
        PSD.logger.error "Unknown image compression: #{@compression}. Attempting to skip."
        @file.seek(@end_pos)
      end
    end

    def parse_user_mask
      return unless has_mask?

      channel = @channels_info.select { |c| c[:id] == -2 }.first
      index = @channels_info.index { |c| c[:id] == -2 }
      return if channel.nil?

      start = @channel_length * index
      length = @layer.mask.width * @layer.mask.height
      PSD.logger.debug "Copying user mask: #{length} bytes at #{start}"

      @mask_data = @channel_data[start, length]

      if @mask_data.length != length
        PSD.logger.error "Mask length is incorrect. Expected = #{length}, Actual = #{@mask_data.length}"
      end
    end
  end
end
