module Optcarrot
  # MMC1 mapper: http://wiki.nesdev.com/w/index.php/MMC1
  class MMC1 < ROM
    MAPPER_DB[0x01] = self

    NMT_MODE = [:second, :first, :vertical, :horizontal]
    PRG_MODE = [:conseq, :conseq, :fix_first, :fix_last]
    CHR_MODE = [:conseq, :noconseq]

    def init
      @nmt_mode = @prg_mode = @chr_mode = nil
      @prg_bank = @chr_bank_0 = @chr_bank_1 = 0
    end

    def reset
      @shift = @shift_count = 0

      @chr_banks = @chr_banks.flatten.each_slice(0x1000).to_a

      @wrk_readable = @wrk_writable = true
      @cpu.add_mappings(0x6000..0x7fff, method(:peek_6000), method(:poke_6000))
      @cpu.add_mappings(0x8000..0xffff, @prg_ref, method(:poke_prg))

      update_nmt(:horizontal)
      update_prg(:fix_last, 0, 0)
      update_chr(:conseq, 0, 0)
    end

    def poke_prg(addr, val)
      if val[7] == 1
        @shift = @shift_count = 0
      else
        @shift |= val[0] << @shift_count
        @shift_count += 1
        if @shift_count == 0x05
          case (addr >> 13) & 0x3
          when 0 # control
            nmt_mode = NMT_MODE[@shift      & 3]
            prg_mode = PRG_MODE[@shift >> 2 & 3]
            chr_mode = CHR_MODE[@shift >> 4 & 1]
            update_nmt(nmt_mode)
            update_prg(prg_mode, @prg_bank, @chr_bank_0)
            update_chr(chr_mode, @chr_bank_0, @chr_bank_1)
          when 1 # change chr_bank_0
            # update_prg might modify @chr_bank_0 and prevent updating chr bank,
            # so keep current value.
            bak_chr_bank_0 = @chr_bank_0
            update_prg(@prg_mode, @prg_bank, @shift)
            @chr_bank_0 = bak_chr_bank_0
            update_chr(@chr_mode, @shift, @chr_bank_1)
          when 2 # change chr_bank_1
            update_chr(@chr_mode, @chr_bank_0, @shift)
          when 3 # change png_bank
            update_prg(@prg_mode, @shift, @chr_bank_0)
          end
          @shift = @shift_count = 0
        end
      end
    end

    def update_nmt(nmt_mode)
      return if @nmt_mode == nmt_mode
      @nmt_mode = nmt_mode
      @ppu.nametables = @nmt_mode
    end

    def update_prg(prg_mode, prg_bank, chr_bank_0)
      return if prg_mode == @prg_mode && prg_bank == @prg_bank && chr_bank_0 == @chr_bank_0
      @prg_mode, @prg_bank, @chr_bank_0 = prg_mode, prg_bank, chr_bank_0

      case @prg_mode
      when :conseq
        lower = (@chr_bank_0 & 0x10) | (@prg_bank & 0x0e)
        upper = lower + 1
      when :fix_first
        lower = 0
        upper = @prg_bank & 0x0f
      when :fix_last
        lower = @prg_bank & 0x0f
        upper = -1
      end
      @prg_ref[0x8000, 0x4000] = @prg_banks[lower]
      @prg_ref[0xc000, 0x4000] = @prg_banks[upper]
    end

    def update_chr(chr_mode, chr_bank_0, chr_bank_1)
      return if @chr_ram
      return if chr_mode == @chr_mode && chr_bank_0 == @chr_bank_0 && chr_bank_1 == @chr_bank_1
      @chr_mode, @chr_bank_0, @chr_bank_1 = chr_mode, chr_bank_0, chr_bank_1

      @ppu.update(0)
      if @chr_mode == :conseq
        lower = @chr_bank_0 & 0x1e
        upper = lower + 1
      else
        lower = @chr_bank_0
        upper = @chr_bank_1
      end
      @chr_ref[0x0000, 0x1000] = @chr_banks[lower]
      @chr_ref[0x1000, 0x1000] = @chr_banks[upper]
    end
  end
end
