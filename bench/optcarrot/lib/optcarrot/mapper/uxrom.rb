module Optcarrot
  # UxROM mapper: http://wiki.nesdev.com/w/index.php/UxROM
  class UxROM < ROM
    MAPPER_DB[0x02] = self

    def reset
      @cpu.add_mappings(0x8000..0xffff, @prg_ref, method(:poke_8000))
    end

    def poke_8000(_addr, data)
      @prg_ref[0x8000, 0x4000] = @prg_banks[data & 7]
    end
  end
end
