require_relative "opt"

module Optcarrot
  # CPU implementation
  class CPU
    NMI_VECTOR   = 0xfffa
    RESET_VECTOR = 0xfffc
    IRQ_VECTOR   = 0xfffe

    IRQ_EXT   = 0x01
    IRQ_FRAME = 0x40
    IRQ_DMC   = 0x80

    CLK_1, CLK_2, CLK_3, CLK_4, CLK_5, CLK_6, CLK_7, CLK_8 = (1..8).map {|i| i * RP2A03_CC }

    def inspect
      "#<#{ self.class }>"
    end

    ###########################################################################
    # initialization

    def initialize(conf)
      @conf = conf

      # load the generated core
      if @conf.load_cpu
        eval(File.read(@conf.load_cpu))
      elsif @conf.opt_cpu
        eval(OptimizedCodeBuilder.new(@conf.loglevel, @conf.opt_cpu).build, nil, "(generated CPU core)")
      end

      # main memory
      @fetch = [nil] * 0x10000
      @store = [nil] * 0x10000
      @peeks = {}
      @pokes = {}
      @ram = [0] * 0x800

      # clock management
      @clk = 0                 # the current clock
      @clk_frame = 0           # the next frame clock
      @clk_target = 0          # the goal clock for the current CPU#run
      @clk_nmi = FOREVER_CLOCK # the next NMI clock (FOREVER_CLOCK means "not scheduled")
      @clk_irq = FOREVER_CLOCK # the next IRQ clock
      @clk_total = 0           # the total elapsed clocks

      # interrupt
      @irq_flags = 0
      @jammed = false

      reset

      # temporary store (valid only during each operation)
      @addr = @data = 0

      @opcode = nil
      @ppu_sync = false
    end

    def reset
      # registers
      @_a = @_x = @_y = 0
      @_sp = 0xfd
      @_pc = 0xfffc

      # P register
      @_p_nz = 1
      @_p_c = 0
      @_p_v = 0
      @_p_i = 0x04
      @_p_d = 0

      # reset clocks
      @clk = @clk_total = 0

      # reset RAM
      @ram.fill(0xff)

      # memory mappings by self
      add_mappings(0x0000..0x07ff, @ram, @ram.method(:[]=))
      add_mappings(0x0800..0x1fff, method(:peek_ram), method(:poke_ram))
      add_mappings(0x2000..0xffff, method(:peek_nop), nil)
      add_mappings(0xfffc, method(:peek_jam_1), nil)
      add_mappings(0xfffd, method(:peek_jam_2), nil)
    end

    def peek_ram(addr)
      @ram[addr % 0x0800]
    end

    def poke_ram(addr, data)
      @ram[addr % 0x0800] = data
    end

    def peek_nop(addr)
      addr >> 8
    end

    def peek_jam_1(_addr)
      @_pc = (@_pc - 1) & 0xffff
      0xfc
    end

    def peek_jam_2(_addr)
      0xff
    end

    ###########################################################################
    # mapped memory API

    def add_mappings(addr, peek, poke)
      # filter the logically equivalent objects
      peek = @peeks[peek] ||= peek
      poke = @pokes[poke] ||= poke

      (addr.is_a?(Integer) ? [addr] : addr).each do |a|
        @fetch[a] = peek
        @store[a] = poke || PokeNop
      end
    end

    def self.poke_nop(_addr, _data)
    end
    PokeNop = method(:poke_nop)

    def fetch(addr)
      @fetch[addr][addr]
    end

    def store(addr, value)
      @store[addr][addr, value]
    end

    def peek16(addr)
      @fetch[addr][addr] + (@fetch[addr + 1][addr + 1] << 8)
    end

    ###########################################################################
    # other APIs

    attr_reader :ram
    attr_writer :apu, :ppu, :ppu_sync

    def current_clock
      @clk
    end

    def next_frame_clock
      @clk_frame
    end

    def next_frame_clock=(clk)
      @clk_frame = clk
      @clk_target = clk if clk < @clk_target
    end

    def steal_clocks(clk)
      @clk += clk
    end

    def odd_clock?
      (@clk_total + @clk) % CLK_2 != 0
    end

    def update
      @apu.clock_dma(@clk)
      @clk
    end

    def dmc_dma(addr)
      # This is inaccurate; it must steal *up to* 4 clocks depending upon
      # whether CPU writes in this clock, but this always steals 4 clocks.
      @clk += CLK_3
      dma_buffer = fetch(addr)
      @clk += CLK_1
      dma_buffer
    end

    def sprite_dma(addr, sp_ram)
      256.times {|i| sp_ram[i] = @ram[addr + i] }
      64.times {|i| sp_ram[i * 4 + 2] &= 0xe3 }
    end

    def boot
      @clk = CLK_7
      @_pc = peek16(RESET_VECTOR)
    end

    def vsync
      @ppu.sync(@clk) if @ppu_sync

      @clk -= @clk_frame
      @clk_total += @clk_frame

      @clk_nmi -= @clk_frame if @clk_nmi != FOREVER_CLOCK
      @clk_irq -= @clk_frame if @clk_irq != FOREVER_CLOCK
      @clk_irq = 0 if @clk_irq < 0
    end

    ###########################################################################
    # interrupts

    def clear_irq(line)
      old_irq_flags = @irq_flags & (IRQ_FRAME | IRQ_DMC)
      @irq_flags &= line ^ (IRQ_EXT | IRQ_FRAME | IRQ_DMC)
      @clk_irq = FOREVER_CLOCK if @irq_flags == 0
      old_irq_flags
    end

    def next_interrupt_clock(clk)
      clk += CLK_1 + CLK_1 / 2 # interrupt edge
      @clk_target = clk if @clk_target > clk
      clk
    end

    def do_irq(line, clk)
      @irq_flags |= line
      @clk_irq = next_interrupt_clock(clk) if @clk_irq == FOREVER_CLOCK && @_p_i == 0
    end

    def do_nmi(clk)
      @clk_nmi = next_interrupt_clock(clk) if @clk_nmi == FOREVER_CLOCK
    end

    def do_isr(vector)
      return if @jammed
      push16(@_pc)
      push8(flags_pack)
      @_p_i = 0x04
      @clk += CLK_7
      addr = vector == NMI_VECTOR ? NMI_VECTOR : fetch_irq_isr_vector
      @_pc = peek16(addr)
    end

    def fetch_irq_isr_vector
      fetch(0x3000) if @clk >= @clk_frame
      if @clk_nmi != FOREVER_CLOCK
        if @clk_nmi + CLK_2 <= @clk
          @clk_nmi = FOREVER_CLOCK
          return NMI_VECTOR
        end
        @clk_nmi = @clk + 1
      end
      return IRQ_VECTOR
    end

    ###########################################################################
    # instruction helpers

    ### P regeister ###

    def flags_pack
      # NVssDIZC
      ((@_p_nz | @_p_nz >> 1) & 0x80) | # N: Negative
        (@_p_nz & 0xff != 0 ? 0 : 2) |  # Z: Zero
        @_p_c |                         # C: Carry
        (@_p_v != 0 ? 0x40 : 0) |       # V: Overflow
        @_p_i |                         # I: Inerrupt
        @_p_d |                         # D: Decimal
        0x20
    end

    def flags_unpack(f)
      @_p_nz = (~f & 2) | ((f & 0x80) << 1)
      @_p_c = f & 0x01
      @_p_v = f & 0x40
      @_p_i = f & 0x04
      @_p_d = f & 0x08
    end

    ### branch helper ###
    def branch(cond)
      if cond
        tmp = @_pc + 1
        rel = fetch(@_pc)
        @_pc = (tmp + (rel < 128 ? rel : rel | 0xff00)) & 0xffff
        @clk += tmp[8] == @_pc[8] ? CLK_3 : CLK_4
      else
        @_pc += 1
        @clk += CLK_2
      end
    end

    ### storers ###
    def store_mem
      store(@addr, @data)
      @clk += CLK_1
    end

    def store_zpg
      @ram[@addr] = @data
    end

    ### stack management ###
    def push8(data)
      @ram[0x0100 + @_sp] = data
      @_sp = (@_sp - 1) & 0xff
    end

    def push16(data)
      push8(data >> 8)
      push8(data & 0xff)
    end

    def pull8
      @_sp = (@_sp + 1) & 0xff
      @ram[0x0100 + @_sp]
    end

    def pull16
      pull8 + 256 * pull8
    end

    ###########################################################################
    # addressing modes

    # immediate addressing (read only)
    def imm(_read, _write)
      @data = fetch(@_pc)
      @_pc += 1
      @clk += CLK_2
    end

    # zero-page addressing
    def zpg(read, write)
      @addr = fetch(@_pc)
      @_pc += 1
      @clk += CLK_3
      if read
        @data = @ram[@addr]
        @clk += CLK_2 if write
      end
    end

    # zero-page indexed addressing
    def zpg_reg(indexed, read, write)
      @addr = (indexed + fetch(@_pc)) & 0xff
      @_pc += 1
      @clk += CLK_4
      if read
        @data = @ram[@addr]
        @clk += CLK_2 if write
      end
    end

    def zpg_x(read, write)
      zpg_reg(@_x, read, write)
    end

    def zpg_y(read, write)
      zpg_reg(@_y, read, write)
    end

    # absolute addressing
    def abs(read, write)
      @addr = peek16(@_pc)
      @_pc += 2
      @clk += CLK_3
      read_write(read, write)
    end

    # absolute indexed addressing
    def abs_reg(indexed, read, write)
      addr = @_pc + 1
      i = indexed + fetch(@_pc)
      @addr = ((fetch(addr) << 8) + i) & 0xffff
      if write
        addr = (@addr - (i & 0x100)) & 0xffff
        fetch(addr)
        @clk += CLK_4
      else
        @clk += CLK_3
        if i & 0x100 != 0
          addr = (@addr - 0x100) & 0xffff # for inlining fetch
          fetch(addr)
          @clk += CLK_1
        end
      end
      read_write(read, write)
      @_pc += 2
    end

    def abs_x(read, write)
      abs_reg(@_x, read, write)
    end

    def abs_y(read, write)
      abs_reg(@_y, read, write)
    end

    # indexed indirect addressing
    def ind_x(read, write)
      addr = fetch(@_pc) + @_x
      @_pc += 1
      @clk += CLK_5
      @addr = @ram[addr & 0xff] | @ram[(addr + 1) & 0xff] << 8
      read_write(read, write)
    end

    # indirect indexed addressing
    def ind_y(read, write)
      addr = fetch(@_pc)
      @_pc += 1
      indexed = @ram[addr] + @_y
      @clk += CLK_4
      if write
        @clk += CLK_1
        @addr = (@ram[(addr + 1) & 0xff] << 8) + indexed
        addr = @addr - (indexed & 0x100) # for inlining fetch
        fetch(addr)
      else
        @addr = ((@ram[(addr + 1) & 0xff] << 8) + indexed) & 0xffff
        if indexed & 0x100 != 0
          addr = (@addr - 0x100) & 0xffff # for inlining fetch
          fetch(addr)
          @clk += CLK_1
        end
      end
      read_write(read, write)
    end

    def read_write(read, write)
      if read
        @data = fetch(@addr)
        @clk += CLK_1
        if write
          store(@addr, @data)
          @clk += CLK_1
        end
      end
    end

    ###########################################################################
    # instructions

    # load instructions
    def _lda
      @_p_nz = @_a = @data
    end

    def _ldx
      @_p_nz = @_x = @data
    end

    def _ldy
      @_p_nz = @_y = @data
    end

    # store instructions
    def _sta
      @data = @_a
    end

    def _stx
      @data = @_x
    end

    def _sty
      @data = @_y
    end

    # transfer instructions
    def _tax
      @clk += CLK_2
      @_p_nz = @_x = @_a
    end

    def _tay
      @clk += CLK_2
      @_p_nz = @_y = @_a
    end

    def _txa
      @clk += CLK_2
      @_p_nz = @_a = @_x
    end

    def _tya
      @clk += CLK_2
      @_p_nz = @_a = @_y
    end

    # flow control instructions
    def _jmp_a
      @_pc = peek16(@_pc)
      @clk += CLK_3
    end

    def _jmp_i
      pos = peek16(@_pc)
      low = fetch(pos)
      pos = (pos & 0xff00) | ((pos + 1) & 0x00ff)
      high = fetch(pos)
      @_pc = high * 256 + low
      @clk += CLK_5
    end

    def _jsr
      data = @_pc + 1
      push16(data)
      @_pc = peek16(@_pc)
      @clk += CLK_6
    end

    def _rts
      @_pc = (pull16 + 1) & 0xffff
      @clk += CLK_6
    end

    def _rti
      @clk += CLK_6
      packed = pull8
      @_pc = pull16
      flags_unpack(packed)
      @clk_irq = @irq_flags == 0 || @_p_i != 0 ? FOREVER_CLOCK : @clk_target = 0
    end

    def _bne
      branch(@_p_nz & 0xff != 0)
    end

    def _beq
      branch(@_p_nz & 0xff == 0)
    end

    def _bmi
      branch(@_p_nz & 0x180 != 0)
    end

    def _bpl
      branch(@_p_nz & 0x180 == 0)
    end

    def _bcs
      branch(@_p_c != 0)
    end

    def _bcc
      branch(@_p_c == 0)
    end

    def _bvs
      branch(@_p_v != 0)
    end

    def _bvc
      branch(@_p_v == 0)
    end

    # math operations
    def _adc
      tmp = @_a + @data + @_p_c
      @_p_v = ~(@_a ^ @data) & (@_a ^ tmp) & 0x80
      @_p_nz = @_a = tmp & 0xff
      @_p_c = tmp[8]
    end

    def _sbc
      data = @data ^ 0xff
      tmp = @_a + data + @_p_c
      @_p_v = ~(@_a ^ data) & (@_a ^ tmp) & 0x80
      @_p_nz = @_a = tmp & 0xff
      @_p_c = tmp[8]
    end

    # logical operations
    def _and
      @_p_nz = @_a &= @data
    end

    def _ora
      @_p_nz = @_a |= @data
    end

    def _eor
      @_p_nz = @_a ^= @data
    end

    def _bit
      @_p_nz = ((@data & @_a) != 0 ? 1 : 0) | ((@data & 0x80) << 1)
      @_p_v = @data & 0x40
    end

    def _cmp
      data = @_a - @data
      @_p_nz = data & 0xff
      @_p_c = 1 - data[8]
    end

    def _cpx
      data = @_x - @data
      @_p_nz = data & 0xff
      @_p_c = 1 - data[8]
    end

    def _cpy
      data = @_y - @data
      @_p_nz = data & 0xff
      @_p_c = 1 - data[8]
    end

    # shift operations
    def _asl
      @_p_c = @data >> 7
      @data = @_p_nz = @data << 1 & 0xff
    end

    def _lsr
      @_p_c = @data & 1
      @data = @_p_nz = @data >> 1
    end

    def _rol
      @_p_nz = (@data << 1 & 0xff) | @_p_c
      @_p_c = @data >> 7
      @data = @_p_nz
    end

    def _ror
      @_p_nz = (@data >> 1) | (@_p_c << 7)
      @_p_c = @data & 1
      @data = @_p_nz
    end

    # increment and decrement operations
    def _dec
      @data = @_p_nz = (@data - 1) & 0xff
    end

    def _inc
      @data = @_p_nz = (@data + 1) & 0xff
    end

    def _dex
      @clk += CLK_2
      @data = @_p_nz = @_x = (@_x - 1) & 0xff
    end

    def _dey
      @clk += CLK_2
      @data = @_p_nz = @_y = (@_y - 1) & 0xff
    end

    def _inx
      @clk += CLK_2
      @data = @_p_nz = @_x = (@_x + 1) & 0xff
    end

    def _iny
      @clk += CLK_2
      @data = @_p_nz = @_y = (@_y + 1) & 0xff
    end

    # flags instructions
    def _clc
      @clk += CLK_2
      @_p_c = 0
    end

    def _sec
      @clk += CLK_2
      @_p_c = 1
    end

    def _cld
      @clk += CLK_2
      @_p_d = 0
    end

    def _sed
      @clk += CLK_2
      @_p_d = 8
    end

    def _clv
      @clk += CLK_2
      @_p_v = 0
    end

    def _sei
      @clk += CLK_2
      if @_p_i == 0
        @_p_i = 0x04
        @clk_irq = FOREVER_CLOCK
        do_isr(IRQ_VECTOR) if @irq_flags != 0
      end
    end

    def _cli
      @clk += CLK_2
      if @_p_i != 0
        @_p_i = 0
        if @irq_flags != 0
          clk = @clk_irq = @clk + 1
          @clk_target = clk if @clk_target > clk
        end
      end
    end

    # stack operations
    def _pha
      @clk += CLK_3
      push8(@_a)
    end

    def _php
      @clk += CLK_3
      data = flags_pack | 0x10
      push8(data)
    end

    def _pla
      @clk += CLK_4
      @_p_nz = @_a = pull8
    end

    def _plp
      @clk += CLK_4
      i = @_p_i
      flags_unpack(pull8)
      if @irq_flags != 0
        if i > @_p_i
          clk = @clk_irq = @clk + 1
          @clk_target = clk if @clk_target > clk
        elsif i < @_p_i
          @clk_irq = FOREVER_CLOCK
          do_isr(IRQ_VECTOR)
        end
      end
    end

    def _tsx
      @clk += CLK_2
      @_p_nz = @_x = @_sp
    end

    def _txs
      @clk += CLK_2
      @_sp = @_x
    end

    # undocumented instructions, rarely used
    def _anc
      @_p_nz = @_a &= @data
      @_p_c = @_p_nz >> 7
    end

    def _ane
      @_a = (@_a | 0xee) & @_x & @data
      @_p_nz = @_a
    end

    def _arr
      @_a = ((@data & @_a) >> 1) | (@_p_c << 7)
      @_p_nz = @_a
      @_p_c = @_a[6]
      @_p_v = @_a[6] ^ @_a[5]
    end

    def _asr
      @_p_c = @data & @_a & 0x1
      @_p_nz = @_a = (@data & @_a) >> 1
    end

    def _dcp
      @data = (@data - 1) & 0xff
      _cmp
    end

    def _isb
      @data = (@data + 1) & 0xff
      _sbc
    end

    def _las
      @_sp &= @data
      @_p_nz = @_a = @_x = @_sp
    end

    def _lax
      @_p_nz = @_a = @_x = @data
    end

    def _lxa
      @_p_nz = @_a = @_x = @data
    end

    def _rla
      c = @_p_c
      @_p_c = @data >> 7
      @data = (@data << 1 & 0xff) | c
      @_p_nz = @_a &= @data
    end

    def _rra
      c = @_p_c << 7
      @_p_c = @data & 1
      @data = (@data >> 1) | c
      _adc
    end

    def _sax
      @data = @_a & @_x
    end

    def _sbx
      @data = (@_a & @_x) - @data
      @_p_c = (@data & 0xffff) <= 0xff ? 1 : 0
      @_p_nz = @_x = @data & 0xff
    end

    def _sha
      @data = @_a & @_x & ((@addr >> 8) + 1)
    end

    def _shs
      @_sp = @_a & @_x
      @data = @_sp & ((@addr >> 8) + 1)
    end

    def _shx
      @data = @_x & ((@addr >> 8) + 1)
      @addr = (@data << 8) | (@addr & 0xff)
    end

    def _shy
      @data = @_y & ((@addr >> 8) + 1)
      @addr = (@data << 8) | (@addr & 0xff)
    end

    def _slo
      @_p_c = @data >> 7
      @data = @data << 1 & 0xff
      @_p_nz = @_a |= @data
    end

    def _sre
      @_p_c = @data & 1
      @data >>= 1
      @_p_nz = @_a ^= @data
    end

    # nops
    def _nop
    end

    # interrupts
    def _brk
      data = @_pc + 1
      push16(data)
      data = flags_pack | 0x10
      push8(data)
      @_p_i = 0x04
      @clk_irq = FOREVER_CLOCK
      @clk += CLK_7
      addr = fetch_irq_isr_vector # for inlining peek16
      @_pc = peek16(addr)
    end

    def _jam
      @_pc = (@_pc - 1) & 0xffff
      @clk += CLK_2
      unless @jammed
        @jammed = true
        # interrupt reset
        @clk_nmi = FOREVER_CLOCK
        @clk_irq = FOREVER_CLOCK
        @irq_flags = 0
      end
    end

    ###########################################################################
    # default core

    def r_op(instr, mode)
      send(mode, true, false)
      send(instr)
    end

    def w_op(instr, mode, store)
      send(mode, false, true)
      send(instr)
      send(store)
    end

    def rw_op(instr, mode, store)
      send(mode, true, true)
      send(instr)
      send(store)
    end

    def a_op(instr)
      @clk += CLK_2
      @data = @_a
      send(instr)
      @_a = @data
    end

    def no_op(_instr, ops, ticks)
      @_pc += ops
      @clk += ticks * RP2A03_CC
    end

    def do_clock
      clock = @apu.do_clock

      clock = @clk_frame if clock > @clk_frame

      if @clk < @clk_nmi
        clock = @clk_nmi if clock > @clk_nmi
        if @clk < @clk_irq
          clock = @clk_irq if clock > @clk_irq
        else
          @clk_irq = FOREVER_CLOCK
          do_isr(IRQ_VECTOR)
        end
      else
        @clk_nmi = @clk_irq = FOREVER_CLOCK
        do_isr(NMI_VECTOR)
      end
      @clk_target = clock
    end

    def run
      do_clock
      begin
        begin
          @opcode = fetch(@_pc)

          if @conf.loglevel >= 3
            @conf.debug("PC:%04X A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC:%3d : OPCODE:%02X (%d, %d)" % [
              @_pc, @_a, @_x, @_y, flags_pack, @_sp, @clk / 4 % 341, @opcode, @clk, @clk_target
            ])
          end

          @_pc += 1

          send(*DISPATCH[@opcode])

          @ppu.sync(@clk) if @ppu_sync
        end while @clk < @clk_target
        do_clock
      end while @clk < @clk_frame
    end

    ADDRESSING_MODES = {
      ctl: [:imm,   :zpg, :imm, :abs, nil,    :zpg_x, nil,    :abs_x],
      rmw: [:imm,   :zpg, :imm, :abs, nil,    :zpg_y, nil,    :abs_y],
      alu: [:ind_x, :zpg, :imm, :abs, :ind_y, :zpg_x, :abs_y, :abs_x],
      uno: [:ind_x, :zpg, :imm, :abs, :ind_y, :zpg_y, :abs_y, :abs_y],
    }

    DISPATCH = []

    def self.op(opcodes, args)
      opcodes.each do |opcode|
        if args.is_a?(Array) && [:r_op, :w_op, :rw_op].include?(args[0])
          kind, op, mode = args
          mode = ADDRESSING_MODES[mode][opcode >> 2 & 7]
          send_args = [kind, op, mode]
          send_args << (mode.to_s.start_with?("zpg") ? :store_zpg : :store_mem) if kind != :r_op
          DISPATCH[opcode] = send_args
        else
          DISPATCH[opcode] = [*args]
        end
      end
    end

    # load instructions
    op([0xa9, 0xa5, 0xb5, 0xad, 0xbd, 0xb9, 0xa1, 0xb1],       [:r_op, :_lda, :alu])
    op([0xa2, 0xa6, 0xb6, 0xae, 0xbe],                         [:r_op, :_ldx, :rmw])
    op([0xa0, 0xa4, 0xb4, 0xac, 0xbc],                         [:r_op, :_ldy, :ctl])

    # store instructions
    op([0x85, 0x95, 0x8d, 0x9d, 0x99, 0x81, 0x91],             [:w_op, :_sta, :alu])
    op([0x86, 0x96, 0x8e],                                     [:w_op, :_stx, :rmw])
    op([0x84, 0x94, 0x8c],                                     [:w_op, :_sty, :ctl])

    # transfer instructions
    op([0xaa],                                                 :_tax)
    op([0xa8],                                                 :_tay)
    op([0x8a],                                                 :_txa)
    op([0x98],                                                 :_tya)

    # flow control instructions
    op([0x4c],                                                 :_jmp_a)
    op([0x6c],                                                 :_jmp_i)
    op([0x20],                                                 :_jsr)
    op([0x60],                                                 :_rts)
    op([0x40],                                                 :_rti)
    op([0xd0],                                                 :_bne)
    op([0xf0],                                                 :_beq)
    op([0x30],                                                 :_bmi)
    op([0x10],                                                 :_bpl)
    op([0xb0],                                                 :_bcs)
    op([0x90],                                                 :_bcc)
    op([0x70],                                                 :_bvs)
    op([0x50],                                                 :_bvc)

    # math operations
    op([0x69, 0x65, 0x75, 0x6d, 0x7d, 0x79, 0x61, 0x71],       [:r_op, :_adc, :alu])
    op([0xe9, 0xeb, 0xe5, 0xf5, 0xed, 0xfd, 0xf9, 0xe1, 0xf1], [:r_op, :_sbc, :alu])

    # logical operations
    op([0x29, 0x25, 0x35, 0x2d, 0x3d, 0x39, 0x21, 0x31],       [:r_op, :_and, :alu])
    op([0x09, 0x05, 0x15, 0x0d, 0x1d, 0x19, 0x01, 0x11],       [:r_op, :_ora, :alu])
    op([0x49, 0x45, 0x55, 0x4d, 0x5d, 0x59, 0x41, 0x51],       [:r_op, :_eor, :alu])
    op([0x24, 0x2c],                                           [:r_op, :_bit, :alu])
    op([0xc9, 0xc5, 0xd5, 0xcd, 0xdd, 0xd9, 0xc1, 0xd1],       [:r_op, :_cmp, :alu])
    op([0xe0, 0xe4, 0xec],                                     [:r_op, :_cpx, :rmw])
    op([0xc0, 0xc4, 0xcc],                                     [:r_op, :_cpy, :rmw])

    # shift operations
    op([0x0a],                                                 [:a_op, :_asl])
    op([0x06, 0x16, 0x0e, 0x1e],                               [:rw_op, :_asl, :alu])
    op([0x4a],                                                 [:a_op, :_lsr])
    op([0x46, 0x56, 0x4e, 0x5e],                               [:rw_op, :_lsr, :alu])
    op([0x2a],                                                 [:a_op, :_rol])
    op([0x26, 0x36, 0x2e, 0x3e],                               [:rw_op, :_rol, :alu])
    op([0x6a],                                                 [:a_op, :_ror])
    op([0x66, 0x76, 0x6e, 0x7e],                               [:rw_op, :_ror, :alu])

    # increment and decrement operations
    op([0xc6, 0xd6, 0xce, 0xde],                               [:rw_op, :_dec, :alu])
    op([0xe6, 0xf6, 0xee, 0xfe],                               [:rw_op, :_inc, :alu])
    op([0xca],                                                 :_dex)
    op([0x88],                                                 :_dey)
    op([0xe8],                                                 :_inx)
    op([0xc8],                                                 :_iny)

    # flags instructions
    op([0x18],                                                 :_clc)
    op([0x38],                                                 :_sec)
    op([0xd8],                                                 :_cld)
    op([0xf8],                                                 :_sed)
    op([0x58],                                                 :_cli)
    op([0x78],                                                 :_sei)
    op([0xb8],                                                 :_clv)

    # stack operations
    op([0x48],                                                 :_pha)
    op([0x08],                                                 :_php)
    op([0x68],                                                 :_pla)
    op([0x28],                                                 :_plp)
    op([0xba],                                                 :_tsx)
    op([0x9a],                                                 :_txs)

    # undocumented instructions, rarely used
    op([0x0b, 0x2b],                                           [:r_op, :_anc, :uno])
    op([0x8b],                                                 [:r_op, :_ane, :uno])
    op([0x6b],                                                 [:r_op, :_arr, :uno])
    op([0x4b],                                                 [:r_op, :_asr, :uno])
    op([0xc7, 0xd7, 0xc3, 0xd3, 0xcf, 0xdf, 0xdb],             [:rw_op, :_dcp, :alu])
    op([0xe7, 0xf7, 0xef, 0xff, 0xfb, 0xe3, 0xf3],             [:rw_op, :_isb, :alu])
    op([0xbb],                                                 [:r_op, :_las, :uno])
    op([0xa7, 0xb7, 0xaf, 0xbf, 0xa3, 0xb3],                   [:r_op, :_lax, :uno])
    op([0xab],                                                 [:r_op, :_lxa, :uno])
    op([0x27, 0x37, 0x2f, 0x3f, 0x3b, 0x23, 0x33],             [:rw_op, :_rla, :alu])
    op([0x67, 0x77, 0x6f, 0x7f, 0x7b, 0x63, 0x73],             [:rw_op, :_rra, :alu])
    op([0x87, 0x97, 0x8f, 0x83],                               [:w_op, :_sax, :uno])
    op([0xcb],                                                 [:r_op, :_sbx, :uno])
    op([0x9f, 0x93],                                           [:w_op, :_sha, :uno])
    op([0x9b],                                                 [:w_op, :_shs, :uno])
    op([0x9e],                                                 [:w_op, :_shx, :rmw])
    op([0x9c],                                                 [:w_op, :_shy, :ctl])
    op([0x07, 0x17, 0x0f, 0x1f, 0x1b, 0x03, 0x13],             [:rw_op, :_slo, :alu])
    op([0x47, 0x57, 0x4f, 0x5f, 0x5b, 0x43, 0x53],             [:rw_op, :_sre, :alu])

    # nops
    op([0x1a, 0x3a, 0x5a, 0x7a, 0xda, 0xea, 0xfa],             [:no_op, :_nop, 0, 2])
    op([0x80, 0x82, 0x89, 0xc2, 0xe2],                         [:no_op, :_nop, 1, 2])
    op([0x04, 0x44, 0x64],                                     [:no_op, :_nop, 1, 3])
    op([0x14, 0x34, 0x54, 0x74, 0xd4, 0xf4],                   [:no_op, :_nop, 1, 4])
    op([0x0c],                                                 [:no_op, :_nop, 2, 4])
    op([0x1c, 0x3c, 0x5c, 0x7c, 0xdc, 0xfc],                   [:r_op, :_nop, :ctl])

    # interrupts
    op([0x00],                                                 :_brk)
    op([0x02, 0x12, 0x22, 0x32, 0x42, 0x52, 0x62, 0x72, 0x92, 0xb2, 0xd2, 0xf2], :_jam)

    ###########################################################################
    # optimized core generator
    class OptimizedCodeBuilder
      include CodeOptimizationHelper

      OPTIONS = [:method_inlining, :constant_inlining, :ivar_localization, :trivial_branches]

      LOCALIZE_IVARS = [:@addr, :@data, :@_a, :@_x, :@_y, :@_pc, :@_sp, :@fetch, :@store, :@ram, :@opcode]

      def build
        depends(:ivar_localization, :method_inlining)

        mdefs = parse_method_definitions(__FILE__)
        code = build_loop(mdefs)

        # optimize!
        code = cpu_expand_methods(code, mdefs) if @method_inlining
        code = remove_trivial_branches(code) if @trivial_branches
        code = expand_constants(code) if @constant_inlining
        code = localize_instance_variables(code, LOCALIZE_IVARS) if @ivar_localization

        gen(
          "def self.run",
          indent(2, code),
          "end",
        )
      end

      # generate a main code
      def build_loop(mdefs)
        dispatch = gen(
          "case @opcode",
          *DISPATCH.map.with_index do |args, opcode|
            if args.size > 1
              mhd, instr, = args
              code = expand_inline_methods("#{ mhd }(#{ args.drop(1).join(", ") })", mhd, mdefs[mhd])
              code = code.gsub(/send\((\w+), (.*?)\)/) { "#{ $1 }(#{ $2 })" }
              code = code.gsub(/send\((\w+)\)/) { $1 }
              code = code[1..-2].split("; ")
            else
              instr = code = args[0]
            end
            "when 0x%02x # #{ instr }\n" % opcode + indent(2, gen(*code))
          end,
          "end"
        )
        main = mdefs[:run].body.sub("@conf.loglevel >= 3") { @loglevel >= 3 }
        main.sub(/^ *send.*\n/) { indent(4, dispatch) }
      end

      # inline method calls
      def cpu_expand_methods(code, mdefs)
        code = expand_methods(code, mdefs, mdefs.keys.grep(/^_/))
        [
          [:_adc, :_sbc, :_cmp, :store_mem, :store_zpg],
          [:imm, :abs, :zpg, :abs_x, :abs_y, :zpg_x, :zpg_y, :ind_x, :ind_y],
          [:abs_reg, :zpg_reg],
          [:read_write],
          [:do_clock],
          [:do_isr],
          [:branch, :push16],
          [:push8],
        ].each do |meths|
          code = expand_methods(code, mdefs, meths)
        end
        [:fetch, :peek16, :store, :pull16, :pull8].each do |meth|
          code = expand_inline_methods(code, meth, mdefs[meth])
        end
        code
      end

      # inline constants
      def expand_constants(handlers)
        handlers = handlers.gsub(/CLK_(\d+)/) { eval($&) }
        handlers = handlers.gsub(/FOREVER_CLOCK/) { "0xffffffff" }
        handlers
      end
    end
  end
end
