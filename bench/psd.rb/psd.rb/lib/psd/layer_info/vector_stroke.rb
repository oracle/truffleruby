require_relative '../layer_info'

class PSD
  class VectorStroke < LayerInfo
    @key = 'vstk'

    def parse
      version = @file.read_int
      @data = Descriptor.new(@file).parse
    end
  end
end