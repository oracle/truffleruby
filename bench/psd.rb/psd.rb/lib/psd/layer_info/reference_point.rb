require_relative '../layer_info'

class PSD
  class ReferencePoint < LayerInfo
    @key = 'fxrp'

    attr_reader :x, :y

    def parse
      @x = @file.read_double
      @y = @file.read_double

      return self
    end
  end
end