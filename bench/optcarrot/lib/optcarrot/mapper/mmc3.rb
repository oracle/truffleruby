module Optcarrot
  # MMC3 mapper: http://wiki.nesdev.com/w/index.php/MMC3
  class MMC3 < ROM
    MAPPER_DB[0x04] = self

    def init(rev = :B) # rev = :A or :B or :C
      @persistant = rev != :A

      @prg_banks = @prg_banks.flatten.each_slice(0x2000).to_a
      @prg_bank_swap = false

      @chr_banks = @chr_banks.flatten.each_slice(0x0400).to_a
      @chr_bank_mapping = [nil] * 8
      @chr_bank_swap = false
    end

    def reset
      @wrk_readable = true
      @wrk_writable = false

      poke_a000 = @mirroring != :FourScreen ? method(:poke_a000) : nil
      @cpu.add_mappings(0x6000..0x7fff, method(:peek_6000), method(:poke_6000))
      @cpu.add_mappings(0x8000.step(0x9fff, 2), @prg_ref, method(:poke_8000))
      @cpu.add_mappings(0x8001.step(0x9fff, 2), @prg_ref, method(:poke_8001))
      @cpu.add_mappings(0xa000.step(0xbfff, 2), @prg_ref, poke_a000)
      @cpu.add_mappings(0xa001.step(0xbfff, 2), @prg_ref, method(:poke_a001))
      @cpu.add_mappings(0xc000.step(0xdfff, 2), @prg_ref, method(:poke_c000))
      @cpu.add_mappings(0xc001.step(0xdfff, 2), @prg_ref, method(:poke_c001))
      @cpu.add_mappings(0xe000.step(0xffff, 2), @prg_ref, method(:poke_e000))
      @cpu.add_mappings(0xe001.step(0xffff, 2), @prg_ref, method(:poke_e001))

      update_prg(0x8000, 0)
      update_prg(0xa000, 1)
      update_prg(0xc000, -2)
      update_prg(0xe000, -1)
      8.times {|i| update_chr(i * 0x400, i) }

      @clock = 0
      @hold = PPU::RP2C02_CC * 16
      @ppu.monitor_a12_rising_edge(self)
      @cpu.ppu_sync = true

      @count = 0
      @latch = 0
      @reload = false
      @enabled = false
    end

    # prg_bank_swap = F T
    # 0x8000..0x9fff: 0 2
    # 0xa000..0xbfff: 1 1
    # 0xc000..0xdfff: 2 0
    # 0xe000..0xffff: 3 3
    def update_prg(addr, bank)
      bank %= @prg_banks.size
      addr ^= 0x4000 if @prg_bank_swap && addr[13] == 0
      @prg_ref[addr, 0x2000] = @prg_banks[bank]
    end

    def update_chr(addr, bank)
      return if @chr_ram
      idx = addr / 0x400
      bank %= @chr_banks.size
      return if @chr_bank_mapping[idx] == bank
      addr ^= 0x1000 if @chr_bank_swap
      @ppu.update(0)
      @chr_ref[addr, 0x400] = @chr_banks[bank]
      @chr_bank_mapping[idx] = bank
    end

    def poke_8000(_addr, data)
      @reg_select = data & 7
      prg_bank_swap = data[6] == 1
      chr_bank_swap = data[7] == 1

      if prg_bank_swap != @prg_bank_swap
        @prg_bank_swap = prg_bank_swap
        @prg_ref[0x8000, 0x2000], @prg_ref[0xc000, 0x2000] = @prg_ref[0xc000, 0x2000], @prg_ref[0x8000, 0x2000]
      end

      if chr_bank_swap != @chr_bank_swap
        @chr_bank_swap = chr_bank_swap
        unless @chr_ram
          @ppu.update(0)
          @chr_ref.rotate!(0x1000)
          @chr_bank_mapping.rotate!(4)
        end
      end
    end

    def poke_8001(_addr, data)
      if @reg_select < 6
        if @reg_select < 2
          update_chr(@reg_select * 0x0800, data & 0xfe)
          update_chr(@reg_select * 0x0800 + 0x0400, data | 0x01)
        else
          update_chr((@reg_select - 2) * 0x0400 + 0x1000, data)
        end
      else
        update_prg((@reg_select - 6) * 0x2000 + 0x8000, data & 0x3f)
      end
    end

    def poke_a000(_addr, data)
      @ppu.nametables = data[0] == 1 ? :horizontal : :vertical
    end

    def poke_a001(_addr, data)
      @wrk_readable = data[7] == 1
      @wrk_writable = data[6] == 0 && @wrk_readable
    end

    def poke_c000(_addr, data)
      @ppu.update(0)
      @latch = data
    end

    def poke_c001(_addr, _data)
      @ppu.update(0)
      @reload = true
    end

    def poke_e000(_addr, _data)
      @ppu.update(0)
      @enabled = false
      @cpu.clear_irq(CPU::IRQ_EXT)
    end

    def poke_e001(_addr, _data)
      @ppu.update(0)
      @enabled = true
    end

    def vsync
      @clock = @clock > @cpu.next_frame_clock ? @clock - @cpu.next_frame_clock : 0
    end

    def a12_signaled(cycle)
      clk, @clock = @clock, cycle + @hold
      return if cycle < clk
      flag = @persistant || @count > 0
      if @reload
        @reload = false
        @count = @latch
      elsif @count == 0
        @count = @latch
      else
        @count -= 1
      end
      @cpu.do_irq(CPU::IRQ_EXT, cycle) if flag && @count == 0 && @enabled
    end
  end
end
