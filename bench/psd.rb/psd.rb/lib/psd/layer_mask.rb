class PSD
  # Covers parsing the global mask and controls parsing of all the
  # layers/folders in the document.
  class LayerMask
    include Section

    attr_reader :layers, :global_mask

    # Store a reference to the file and the header and initialize the defaults.
    def initialize(file, header, options)
      @file = file
      @header = header
      @options = options

      @layers = []
      @merged_alpha = false
      @global_mask = nil
      @extras = []
    end

    # Allows us to skip this section because it starts with the length of the section
    # stored as an integer.
    def skip
      @file.seek @file.read_int, IO::SEEK_CUR
      return self
    end

    # Parse this section, including all of the layers and folders. Once implemented, this
    # will also trigger parsing of the channel images for each layer.
    def parse
      start_section

      mask_size = @file.read_int
      finish = @file.tell + mask_size

      return self if mask_size <= 0

      layer_info_size = Util.pad2(@file.read_int)

      if layer_info_size > 0
        layer_count = @file.read_short

        if layer_count < 0
          layer_count = layer_count.abs
          @merged_alpha = true
        end

        if layer_count * (18 + 6 * @header.channels) > layer_info_size
          raise "Unlikely number of layers parsed: #{layer_count}"
        end

        @layer_section_start = @file.tell
        layer_count.times do
          @layers << PSD::Layer.new(@file, @header).parse
        end

        layers.each do |layer|
          layer.parse_channel_image(@header)
        end
      end

      # Layers are parsed in reverse order
      layers.reverse!
      group_layers

      parse_global_mask

      # Ensure we're at the end of this section
      @file.seek finish
      end_section

      return self
    end

    # Export the mask and all the children layers to a file.
    def export(outfile)
      if @layers.size == 0
        # No data, just read whatever's here.
        return outfile.write @file.read(@section_end[:all] - start_of_section)
      end

      # Read the initial mask data since it won't change
      outfile.write @file.read(@layer_section_start - @file.tell)

      @layers.reverse.each do |layer|
        layer.export(outfile)
      end

      outfile.write @file.read(end_of_section - @file.tell)
    end

    private

    def group_layers
      group_layer = nil
      layers.each do |layer|
        if layer.folder?
          group_layer = layer
        elsif layer.folder_end?
          group_layer = nil
        else
          layer.group_layer = layer
        end
      end
    end

    def parse_global_mask
      length = @file.read_int
      
      PSD.logger.debug "Global Mask: length = #{length}"
      return if length <= 0

      mask_end = @file.tell + length
      
      @global_mask = {}
      @global_mask[:overlay_color_space] = @file.read_short
      @global_mask[:color_components] = 4.times.map { |i| @file.read_short >> 8 }
      @global_mask[:opacity] = @file.read_short / 16.0

      # 0 = color selected, 1 = color protected, 128 = use value per layer
      @global_mask[:kind] = @file.read(1).bytes.to_a[0]

      PSD.logger.debug @global_mask

      # Filler zeros
      @file.seek mask_end
    end
  end
end