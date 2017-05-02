require_relative '../layer_info'

class PSD
  class MetadataSetting < LayerInfo
    @key = 'shmd'

    def parse
      count = @file.read_int

      count.times do
        @file.seek 4, IO::SEEK_CUR # signature, always 8BIM
        
        key = @file.read_string(4)
        copy_on_sheet_dup = @file.read(1).bytes.to_a[0]
        @file.seek 3, IO::SEEK_CUR # Padding

        len = @file.read_int
        data_end = @file.tell + len

        PSD.logger.debug "Layer metadata: key = #{key}, length = #{len}"

        parse_layer_comp_setting if key == 'cmls'

        @file.seek data_end
      end
    end

    private

    def parse_layer_comp_setting
      @file.seek 4, IO::SEEK_CUR # Version
      @data[:layer_comp] = Descriptor.new(@file).parse
    end
  end
end