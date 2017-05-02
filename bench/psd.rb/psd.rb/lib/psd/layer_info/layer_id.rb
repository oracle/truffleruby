require_relative '../layer_info'

class PSD
  class LayerID < LayerInfo
    @key = 'lyid'

    attr_reader :id

    def parse
      @id = @file.read_int
    end
  end
end