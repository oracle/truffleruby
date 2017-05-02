require_relative '../layer_info'

class PSD
  class BlendInteriorElements < LayerInfo
    @key = 'infx'

    attr_reader :enabled
    def parse
      @enabled = @file.read_boolean
      @file.seek 3, IO::SEEK_CUR
    end
  end
end