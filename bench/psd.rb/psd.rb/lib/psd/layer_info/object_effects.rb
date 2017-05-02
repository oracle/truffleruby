require_relative '../layer_info'

class PSD
  class ObjectEffects < LayerInfo
    @key = 'lfx2'
    
    def parse
      version = @file.read_int
      descriptor_version = @file.read_int

      @data = Descriptor.new(@file).parse

      return self
    end
  end
end