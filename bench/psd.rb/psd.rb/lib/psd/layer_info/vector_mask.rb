require_relative '../layer_info'

class PSD
  class VectorMask < LayerInfo
    @key = 'vmsk'

    attr_reader :invert, :not_link, :disable, :paths

    def parse
      version = @file.read_int
      tag = @file.read_int

      @invert = tag & 0x01
      @not_link = (tag & (0x01 << 1)) > 0
      @disable = (tag & (0x01 << 2)) > 0

      num_records = (@length - 8) / 26
      
      @paths = []
      num_records.times do
        @paths << PathRecord.new(@file)
      end
    end
  end
end