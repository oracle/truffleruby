require_relative '../layer_info'

class PSD
  class LayerSectionDivider < LayerInfo
    @key = 'lsct'

    attr_reader :layer_type, :is_folder, :is_hidden, :blend_mode, :sub_type

    SECTION_DIVIDER_TYPES = [
      "other",
      "open folder",
      "closed folder",
      "bounding section divider"
    ]

    def initialize(file, length)
      super

      @is_folder = false
      @is_hidden = false
      @blend_mode = nil
      @sub_type = nil
    end

    def parse
      code = @file.read_int
      @layer_type = SECTION_DIVIDER_TYPES[code]

      case code
      when 1, 2 then @is_folder = true
      when 3 then @is_hidden = true
      end

      PSD.logger.warn "Section divider is unexpected value: #{code}" if code > 4

      return self unless @length >= 12

      @file.seek 4, IO::SEEK_CUR # sig
      @blend_mode = @file.read_string(4)

      return self unless @length >= 16

      @sub_type = @file.read_int == 0 ? 'normal' : 'scene group'

      return self
    end
  end
end