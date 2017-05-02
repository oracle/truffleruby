require_relative '../layer_info'

class PSD
  class Locked < LayerInfo
    @key = 'lspf'

    attr_reader :all_locked, :transparency_locked, :composite_locked, :position_locked

    def parse
      locked = @file.read_int

      @transparency_locked = (locked & (0x01 << 0)) > 0 || locked == -2147483648
      @composite_locked = (locked & (0x01 << 1)) > 0 || locked == -2147483648
      @position_locked = (locked & (0x01 << 2)) > 0 || locked == -2147483648
      
      @all_locked = @transparency_locked && @composite_locked && @position_locked
    end
  end
end