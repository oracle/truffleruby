require_relative '../layer_info'

class PSD
  class FillOpacity < LayerInfo
    @key = 'iOpa'

    attr_reader :value

    def parse
      @value = @file.read_byte.to_i
    end
  end
end