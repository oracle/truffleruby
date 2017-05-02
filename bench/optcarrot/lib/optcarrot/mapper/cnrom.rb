module Optcarrot
  # CNROM mapper: http://wiki.nesdev.com/w/index.php/CNROM
  class CNROM < ROM
    MAPPER_DB[0x03] = self

    def reset
      @cpu.add_mappings(0x8000..0xffff, @prg_ref, @chr_ram ? nil : method(:poke_8000))
    end

    def poke_8000(_addr, data)
      @chr_ref.replace(@chr_banks[data & 3])
    end
  end
end
