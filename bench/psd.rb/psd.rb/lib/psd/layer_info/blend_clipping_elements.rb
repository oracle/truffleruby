require_relative '../layer_info'

class PSD
  class BlendClippingElements < LayerInfo
    @key = 'clbl'

    attr_reader :enabled
    def parse
      @enabled = @file.read_boolean
      @file.seek 3, IO::SEEK_CUR
    end
  end
end