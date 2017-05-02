require_relative '../layer_info'

class PSD
  class LayerNameSource < LayerInfo
    @key = 'lnsr'
    
    attr_reader :id
    
    def parse
      @id = @file.read_string(4)
      return self
    end
  end
end