require_relative "opt"

module Optcarrot
  # PPU implementation (video output)
  class PPU
    # clock/timing constants (stolen from Nestopia)
    RP2C02_CC         = 4
    RP2C02_HACTIVE    = RP2C02_CC * 256
    RP2C02_HBLANK     = RP2C02_CC * 85
    RP2C02_HSYNC      = RP2C02_HACTIVE + RP2C02_HBLANK
    RP2C02_VACTIVE    = 240
    RP2C02_VSLEEP     = 1
    RP2C02_VINT       = 20
    RP2C02_VDUMMY     = 1
    RP2C02_VBLANK     = RP2C02_VSLEEP + RP2C02_VINT + RP2C02_VDUMMY
    RP2C02_VSYNC      = RP2C02_VACTIVE + RP2C02_VBLANK
    RP2C02_HVSYNCBOOT = RP2C02_VACTIVE * RP2C02_HSYNC + RP2C02_CC * 312
    RP2C02_HVINT      = RP2C02_VINT * RP2C02_HSYNC
    RP2C02_HVSYNC_0   = RP2C02_VSYNC * RP2C02_HSYNC
    RP2C02_HVSYNC_1   = RP2C02_VSYNC * RP2C02_HSYNC - RP2C02_CC

    # special scanlines
    SCANLINE_HDUMMY = -1  # pre-render scanline
    SCANLINE_VBLANK = 240 # post-render scanline

    # special horizontal clocks
    HCLOCK_DUMMY    = 341
    HCLOCK_VBLANK_0 = 681
    HCLOCK_VBLANK_1 = 682
    HCLOCK_VBLANK_2 = 684
    HCLOCK_BOOT     = 685
    DUMMY_FRAME = [RP2C02_HVINT / RP2C02_CC - HCLOCK_DUMMY, RP2C02_HVINT, RP2C02_HVSYNC_0]
    BOOT_FRAME = [RP2C02_HVSYNCBOOT / RP2C02_CC - HCLOCK_BOOT, RP2C02_HVSYNCBOOT, RP2C02_HVSYNCBOOT]

    # constants related to OAM (sprite)
    SP_PIXEL_POSITIONS = {
      0 => [3, 7, 2, 6, 1, 5, 0, 4], # normal
      1 => [4, 0, 5, 1, 6, 2, 7, 3], # flip
    }

    # A look-up table mapping: (two pattern bytes * attr) -> eight pixels
    #   TILE_LUT[attr][high_byte * 0x100 + low_byte] = [pixels] * 8
    TILE_LUT = [0x0, 0x4, 0x8, 0xc].map do |attr|
      (0..7).map do |j|
        (0...0x10000).map do |i|
          clr = i[15 - j] * 2 + i[7 - j]
          clr != 0 ? attr | clr : 0
        end
      end.transpose
      # Super dirty hack: This Array#transpose reduces page-faults.
      # It might generate cache-friendly memory layout...
    end

    def inspect
      "#<#{ self.class }>"
    end

    ###########################################################################
    # initialization

    def initialize(conf, cpu, palette)
      @conf = conf
      @cpu = cpu
      @palette = palette

      if @conf.load_ppu
        eval(File.read(@conf.load_ppu))
      elsif @conf.opt_ppu
        eval(OptimizedCodeBuilder.new(@conf.loglevel, @conf.opt_ppu).build, nil, "(generated PPU core)")
      end

      @nmt_mem = [[0xff] * 0x400, [0xff] * 0x400]
      @nmt_ref = [0, 1, 0, 1].map {|i| @nmt_mem[i] }

      @output_pixels = []
      @output_color = [@palette[0]] * 0x20 # palette size is 0x20

      reset(mapping: false)
      setup_lut
    end

    def reset(opt = {})
      if opt.fetch(:mapping, true)
        # setup mapped memory
        @cpu.add_mappings(0x2000.step(0x3fff, 8), method(:peek_2xxx), method(:poke_2000))
        @cpu.add_mappings(0x2001.step(0x3fff, 8), method(:peek_2xxx), method(:poke_2001))
        @cpu.add_mappings(0x2002.step(0x3fff, 8), method(:peek_2002), method(:poke_2xxx))
        @cpu.add_mappings(0x2003.step(0x3fff, 8), method(:peek_2xxx), method(:poke_2003))
        @cpu.add_mappings(0x2004.step(0x3fff, 8), method(:peek_2004), method(:poke_2004))
        @cpu.add_mappings(0x2005.step(0x3fff, 8), method(:peek_2xxx), method(:poke_2005))
        @cpu.add_mappings(0x2006.step(0x3fff, 8), method(:peek_2xxx), method(:poke_2006))
        @cpu.add_mappings(0x2007.step(0x3fff, 8), method(:peek_2007), method(:poke_2007))
        @cpu.add_mappings(0x3000, method(:peek_3000), method(:poke_2000))
        @cpu.add_mappings(0x4014, method(:peek_4014), method(:poke_4014))
      end

      @palette_ram = [
        0x3f, 0x01, 0x00, 0x01, 0x00, 0x02, 0x02, 0x0d,
        0x08, 0x10, 0x08, 0x24, 0x00, 0x00, 0x04, 0x2c,
        0x09, 0x01, 0x34, 0x03, 0x00, 0x04, 0x00, 0x14,
        0x08, 0x3a, 0x00, 0x02, 0x00, 0x20, 0x2c, 0x08,
      ]
      @coloring = 0x3f # not monochrome
      @emphasis = 0
      update_output_color

      # clock management
      @hclk = HCLOCK_BOOT
      @vclk = 0
      @hclk_target = FOREVER_CLOCK

      # CPU-PPU interface
      @io_latch = 0
      @io_buffer = 0xe8 # garbage

      @regs_oam = 0

      # misc
      @vram_addr_inc = 1 # 1 or 32
      @need_nmi = false
      @pattern_end = 0x0ff0
      @any_show = false # == @bg_show || @sp_show
      @sp_overflow = false
      @sp_zero_hit = false
      @vblanking = @vblank = false

      # PPU-nametable interface
      @io_addr = 0
      @io_pattern = 0

      @a12_monitor = nil
      @a12_state = nil

      # the current scanline
      @odd_frame = false
      @scanline = SCANLINE_VBLANK

      # scroll state
      @scroll_toggle = false
      @scroll_latch = 0
      @scroll_xfine = 0
      @scroll_addr_0_4 = @scroll_addr_5_14 = 0
      @name_io_addr = 0x2000 # == (@scroll_addr_0_4 | @scroll_addr_5_14) & 0x0fff | 0x2000

      ### BG-sprite state
      @bg_enabled = false
      @bg_show = false
      @bg_show_edge = false
      @bg_pixels = [0] * 16
      @bg_pattern_base = 0 # == 0 or 0x1000
      @bg_pattern_base_15 = 0 # == @bg_pattern_base[12] << 15
      @bg_pattern = 0
      @bg_pattern_lut = TILE_LUT[0]
      @bg_pattern_lut_fetched = TILE_LUT[0]
      # invariant:
      #   @bg_pattern_lut_fetched == TILE_LUT[
      #     @nmt_ref[@io_addr >> 10 & 3][@io_addr & 0x03ff] >>
      #       ((@scroll_addr_0_4 & 0x2) | (@scroll_addr_5_14[6] * 0x4)) & 3
      #   ]

      ### OAM-sprite state
      @sp_enabled = false
      @sp_active = false # == @sp_visible && @sp_enabled
      @sp_show = false
      @sp_show_edge = false

      # for CPU-PPU interface
      @sp_base = 0
      @sp_height = 8

      # for OAM fetcher
      @sp_phase = 0
      @sp_ram = [0xff] * 0x100 # ram size is 0x100, 0xff is a OAM garbage
      @sp_index = 0
      @sp_addr = 0
      @sp_latch = 0

      # for internal state
      # 8 sprites per line are allowed in standard NES, but a user may remove this limit.
      @sp_limit = (@conf.sprite_limit ? 8 : 32) * 4
      @sp_buffer = [0] * @sp_limit
      @sp_buffered = 0
      @sp_visible = false
      @sp_map = [nil] * 264 # [[behind?, zero?, color]]
      @sp_map_buffer = (0...264).map { [false, false, 0] } # preallocation for @sp_map
      @sp_zero_in_line = false
    end

    def update_output_color
      0x20.times do |i|
        @output_color[i] = @palette[@palette_ram[i] & @coloring | @emphasis]
      end
    end

    def setup_lut
      @lut_update = {}.compare_by_identity

      @name_lut = (0..0xffff).map do |i|
        nmt_bank = @nmt_ref[i >> 10 & 3]
        nmt_idx = i & 0x03ff
        fixed = (i >> 12 & 7) | (i[15] << 12)
        (((@lut_update[nmt_bank] ||= [])[nmt_idx] ||= [nil, nil])[0] ||= []) << [i, fixed]
        nmt_bank[nmt_idx] << 4 | fixed
      end

      entries = {}
      @attr_lut = (0..0x7fff).map do |i|
        io_addr = 0x23c0 | (i & 0x0c00) | (i >> 4 & 0x0038) | (i >> 2 & 0x0007)
        nmt_bank = @nmt_ref[io_addr >> 10 & 3]
        nmt_idx = io_addr & 0x03ff
        attr_shift = (i & 2) | (i >> 4 & 4)
        key = [io_addr, attr_shift]
        entries[key] ||= [io_addr, TILE_LUT[nmt_bank[nmt_idx] >> attr_shift & 3], attr_shift]
        (((@lut_update[nmt_bank] ||= [])[nmt_idx] ||= [nil, nil])[1] ||= []) << entries[key]
        entries[key]
      end.freeze
      entries.each_value {|a| a.uniq! {|entry| entry.object_id } }
    end

    ###########################################################################
    # other APIs

    attr_reader :output_pixels

    def set_chr_mem(mem, writable)
      @chr_mem = mem
      @chr_mem_writable = writable
    end

    NMT_TABLE = {
      horizontal:  [0, 0, 1, 1],
      vertical:    [0, 1, 0, 1],
      four_screen: [0, 1, 2, 3],
      first:       [0, 0, 0, 0],
      second:      [1, 1, 1, 1],
    }
    def nametables=(mode)
      update(RP2C02_CC)
      idxs = NMT_TABLE[mode]
      return if (0..3).all? {|i| @nmt_ref[i].equal?(@nmt_mem[idxs[i]]) }
      @nmt_ref[0] = @nmt_mem[idxs[0]]
      @nmt_ref[1] = @nmt_mem[idxs[1]]
      @nmt_ref[2] = @nmt_mem[idxs[2]]
      @nmt_ref[3] = @nmt_mem[idxs[3]]
      setup_lut
    end

    def update(data_setup)
      sync(data_setup + @cpu.update)
    end

    def setup_frame
      @output_pixels.clear
      @odd_frame = !@odd_frame
      @vclk, @hclk_target, @cpu.next_frame_clock = @hclk == HCLOCK_DUMMY ? DUMMY_FRAME : BOOT_FRAME
    end

    def vsync
      if @hclk_target != FOREVER_CLOCK
        @hclk_target = FOREVER_CLOCK
        run
      end
      @output_pixels << @palette[15] while @output_pixels.size < 256 * 240 # fill black
    end

    def monitor_a12_rising_edge(monitor)
      @a12_monitor = monitor
    end

    ###########################################################################
    # helpers

    def update_vram_addr
      if @vram_addr_inc == 32
        if active?
          if @scroll_addr_5_14 & 0x7000 == 0x7000
            @scroll_addr_5_14 &= 0x0fff
            case @scroll_addr_5_14 & 0x03e0
            when 0x03a0 then @scroll_addr_5_14 ^= 0x0800
            when 0x03e0 then @scroll_addr_5_14 &= 0x7c00
            else             @scroll_addr_5_14 += 0x20
            end
          else
            @scroll_addr_5_14 += 0x1000
          end
        else
          @scroll_addr_5_14 += 0x20
        end
      elsif @scroll_addr_0_4 < 0x1f
        @scroll_addr_0_4 += 1
      else
        @scroll_addr_0_4 = 0
        @scroll_addr_5_14 += 0x20
      end
      update_scroll_address_line
    end

    def update_scroll_address_line
      @name_io_addr = (@scroll_addr_0_4 | @scroll_addr_5_14) & 0x0fff | 0x2000
      if @a12_monitor
        a12_state = @scroll_addr_5_14 & 0x3000 == 0x1000
        @a12_monitor.a12_signaled(@cpu.current_clock) if !@a12_state && a12_state
        @a12_state = a12_state
      end
    end

    def active?
      @scanline != SCANLINE_VBLANK && @any_show
    end

    def sync(elapsed)
      return unless @hclk_target < elapsed
      @hclk_target = elapsed / RP2C02_CC - @vclk
      run
    end

    def make_sure_invariants
      @name_io_addr = (@scroll_addr_0_4 | @scroll_addr_5_14) & 0x0fff | 0x2000
      @bg_pattern_lut_fetched = TILE_LUT[
        @nmt_ref[@io_addr >> 10 & 3][@io_addr & 0x03ff] >> ((@scroll_addr_0_4 & 0x2) | (@scroll_addr_5_14[6] * 0x4)) & 3
      ]
    end

    def io_latch_mask(data)
      if active?
        0xff
      elsif @regs_oam & 0x03 == 0x02
        data & 0xe3
      else
        data
      end
    end

    ###########################################################################
    # mapped memory handlers

    # PPUCTRL
    def poke_2000(_addr, data)
      update(RP2C02_CC)
      need_nmi_old = @need_nmi

      @scroll_latch    = (@scroll_latch & 0x73ff) | (data & 0x03) << 10
      @vram_addr_inc   = data[2] == 1 ? 32 : 1
      @sp_base         = data[3] == 1 ? 0x1000 : 0x0000
      @bg_pattern_base = data[4] == 1 ? 0x1000 : 0x0000
      @sp_height       = data[5] == 1 ? 16 : 8
      @need_nmi        = data[7] == 1

      @io_latch = data
      @pattern_end = @sp_base != 0 || @sp_height == 16 ? 0x1ff0 : 0x0ff0
      @bg_pattern_base_15 = @bg_pattern_base[12] << 15

      if @need_nmi && @vblank && !need_nmi_old
        clock = @cpu.current_clock + RP2C02_CC
        @cpu.do_nmi(clock) if clock < RP2C02_HVINT
      end
    end

    # PPUMASK
    def poke_2001(_addr, data)
      update(RP2C02_CC)
      bg_show_old, bg_show_edge_old = @bg_show, @bg_show_edge
      sp_show_old, sp_show_edge_old = @sp_show, @sp_show_edge
      any_show_old = @any_show
      coloring_old, emphasis_old = @coloring, @emphasis

      @bg_show      = data[3] == 1
      @bg_show_edge = data[1] == 1 && @bg_show
      @sp_show      = data[4] == 1
      @sp_show_edge = data[2] == 1 && @sp_show
      @any_show = @bg_show || @sp_show
      @coloring = data[0] == 1 ? 0x30 : 0x3f # 0x30: monochrome
      @emphasis = (data & 0xe0) << 1

      @io_latch = data

      if bg_show_old != @bg_show || bg_show_edge_old != @bg_show_edge ||
         sp_show_old != @sp_show || sp_show_edge_old != @sp_show_edge

        if @hclk < 8 || @hclk >= 248
          update_enabled_flags_edge
        else
          update_enabled_flags
        end
        update_scroll_address_line if any_show_old && !@any_show
      end

      update_output_color if coloring_old != @coloring || emphasis_old != @emphasis
    end

    # PPUSTATUS
    def peek_2002(_addr)
      update(RP2C02_CC)
      v = @io_latch & 0x1f
      v |= 0x80 if @vblank
      v |= 0x40 if @sp_zero_hit
      v |= 0x20 if @sp_overflow
      @io_latch = v
      @scroll_toggle = false
      @vblanking = @vblank = false
      @io_latch
    end

    # OAMADDR
    def poke_2003(_addr, data)
      update(RP2C02_CC)
      @regs_oam = @io_latch = data
    end

    # OAMDATA (write)
    def poke_2004(_addr, data)
      update(RP2C02_CC)
      @io_latch = @sp_ram[@regs_oam] = io_latch_mask(data)
      @regs_oam = (@regs_oam + 1) & 0xff
    end

    # OAMDATA (read)
    def peek_2004(_addr)
      if !@any_show || @cpu.current_clock - (@cpu.next_frame_clock - (341 * 241) * RP2C02_CC) >= (341 * 240) * RP2C02_CC
        @io_latch = @sp_ram[@regs_oam]
      else
        update(RP2C02_CC)
        @io_latch = @sp_latch
      end
    end

    # PPUSCROLL
    def poke_2005(_addr, data)
      update(RP2C02_CC)
      @io_latch = data
      @scroll_toggle = !@scroll_toggle
      if @scroll_toggle
        @scroll_latch = @scroll_latch & 0x7fe0 | (data >> 3)
        xfine = 8 - (data & 0x7)
        @bg_pixels.rotate!(@scroll_xfine - xfine)
        @scroll_xfine = xfine
      else
        @scroll_latch = (@scroll_latch & 0x0c1f) | ((data << 2 | data << 12) & 0x73e0)
      end
    end

    # PPUADDR
    def poke_2006(_addr, data)
      update(RP2C02_CC)
      @io_latch = data
      @scroll_toggle = !@scroll_toggle
      if @scroll_toggle
        @scroll_latch = @scroll_latch & 0x00ff | (data & 0x3f) << 8
      else
        @scroll_latch = (@scroll_latch & 0x7f00) | data
        @scroll_addr_0_4  = @scroll_latch & 0x001f
        @scroll_addr_5_14 = @scroll_latch & 0x7fe0
        update_scroll_address_line
      end
    end

    # PPUDATA (write)
    def poke_2007(_addr, data)
      update(RP2C02_CC * 4)
      addr = @scroll_addr_0_4 | @scroll_addr_5_14
      update_vram_addr
      @io_latch = data
      if addr & 0x3f00 == 0x3f00
        addr &= 0x1f
        final = @palette[data & @coloring | @emphasis]
        @palette_ram[addr] = data
        @output_color[addr] = final
        if addr & 3 == 0
          @palette_ram[addr ^ 0x10] = data
          @output_color[addr ^ 0x10] = final
        end
        @output_bg_color = @palette_ram[0] & 0x3f
      else
        addr &= 0x3fff
        if addr >= 0x2000
          nmt_bank = @nmt_ref[addr >> 10 & 0x3]
          nmt_idx = addr & 0x03ff
          if nmt_bank[nmt_idx] != data
            nmt_bank[nmt_idx] = data

            name_lut_update, attr_lut_update = @lut_update[nmt_bank][nmt_idx]
            name_lut_update.each {|i, b| @name_lut[i] = data << 4 | b } if name_lut_update
            attr_lut_update.each {|a| a[1] = TILE_LUT[data >> a[2] & 3] } if attr_lut_update
          end
        elsif @chr_mem_writable
          @chr_mem[addr] = data
        end
      end
    end

    # PPUDATA (read)
    def peek_2007(_addr)
      update(RP2C02_CC)
      addr = (@scroll_addr_0_4 | @scroll_addr_5_14) & 0x3fff
      update_vram_addr
      @io_latch = (addr & 0x3f00) != 0x3f00 ? @io_buffer : @palette_ram[addr & 0x1f] & @coloring
      @io_buffer = addr >= 0x2000 ? @nmt_ref[addr >> 10 & 0x3][addr & 0x3ff] : @chr_mem[addr]
      @io_latch
    end

    def poke_2xxx(_addr, data)
      @io_latch = data
    end

    def peek_2xxx(_addr)
      @io_latch
    end

    def peek_3000(_addr)
      update(RP2C02_CC)
      @io_latch
    end

    # OAMDMA
    def poke_4014(_addr, data) # DMA
      @cpu.steal_clocks(CPU::CLK_1) if @cpu.odd_clock?
      update(RP2C02_CC)
      @cpu.steal_clocks(CPU::CLK_1)
      data <<= 8
      if @regs_oam == 0 && data < 0x2000 && (!@any_show || @cpu.current_clock <= RP2C02_HVINT - CPU::CLK_1 * 512)
        @cpu.steal_clocks(CPU::CLK_1 * 512)
        @cpu.sprite_dma(data & 0x7ff, @sp_ram)
        @io_latch = @sp_ram[0xff]
      else
        begin
          @io_latch = @cpu.fetch(data)
          data += 1
          @cpu.steal_clocks(CPU::CLK_1)
          update(RP2C02_CC)
          @cpu.steal_clocks(CPU::CLK_1)
          @io_latch = io_latch_mask(@io_latch)
          @sp_ram[@regs_oam] = @io_latch
          @regs_oam = (@regs_oam + 1) & 0xff
        end while data & 0xff != 0
      end
    end

    def peek_4014(_addr)
      0x40
    end

    ###########################################################################
    # helper methods for PPU#run

    # NOTE: These methods will be adhocly-inlined.  Keep compatibility with
    # OptimizedCodeBuilder (e.g., do not change the parameter names blindly).

    def open_pattern(exp)
      return unless @any_show
      @io_addr = exp
      update_address_line
    end

    def open_sprite(buffer_idx)
      flip_v = @sp_buffer[buffer_idx + 2][7] # OAM byte2 bit7: "Flip vertically" flag
      tmp = (@scanline - @sp_buffer[buffer_idx]) ^ (flip_v * 0xf)
      byte1 = @sp_buffer[buffer_idx + 1]
      addr = @sp_height == 16 ? ((byte1 & 0x01) << 12) | ((byte1 & 0xfe) << 4) | (tmp[3] * 0x10) : @sp_base | byte1 << 4
      addr | (tmp & 7)
    end

    def load_sprite(pat0, pat1, buffer_idx)
      byte2 = @sp_buffer[buffer_idx + 2]
      pos = SP_PIXEL_POSITIONS[byte2[6]] # OAM byte2 bit6: "Flip horizontally" flag
      pat = (pat0 >> 1 & 0x55) | (pat1 & 0xaa) | ((pat0 & 0x55) | (pat1 << 1 & 0xaa)) << 8
      x_base = @sp_buffer[buffer_idx + 3]
      palette_base = 0x10 + ((byte2 & 3) << 2) # OAM byte2 bit0-1: Palette
      @sp_visible = @sp_map.clear unless @sp_visible
      8.times do |dx|
        x = x_base + dx
        clr = (pat >> (pos[dx] * 2)) & 3
        next if @sp_map[x] || clr == 0
        @sp_map[x] = sprite = @sp_map_buffer[x]
        # sprite[0]: behind flag, sprite[1]: zero hit flag, sprite[2]: color
        sprite[0] = byte2[5] == 1 # OAM byte2 bit5: "Behind background" flag
        sprite[1] = buffer_idx == 0 && @sp_zero_in_line
        sprite[2] = palette_base + clr
      end
      @sp_active = @sp_enabled
    end

    def update_address_line
      if @a12_monitor
        a12_state = @io_addr[12] == 1
        @a12_monitor.a12_signaled((@vclk + @hclk) * RP2C02_CC) if !@a12_state && a12_state
        @a12_state = a12_state
      end
    end

    ###########################################################################
    # actions for PPU#run

    def open_name
      return unless @any_show
      @io_addr = @name_io_addr
      update_address_line
    end

    def fetch_name
      return unless @any_show
      @io_pattern = @name_lut[@scroll_addr_0_4 + @scroll_addr_5_14 + @bg_pattern_base_15]
    end

    def open_attr
      return unless @any_show
      @io_addr, @bg_pattern_lut_fetched, = @attr_lut[@scroll_addr_0_4 + @scroll_addr_5_14]
      update_address_line
    end

    def fetch_attr
      return unless @any_show
      @bg_pattern_lut = @bg_pattern_lut_fetched
      # raise unless @bg_pattern_lut_fetched ==
      #   @nmt_ref[@io_addr >> 10 & 3][@io_addr & 0x03ff] >>
      #     ((@scroll_addr_0_4 & 0x2) | (@scroll_addr_5_14[6] * 0x4)) & 3
    end

    def fetch_bg_pattern_0
      return unless @any_show
      @bg_pattern = @chr_mem[@io_addr & 0x1fff]
    end

    def fetch_bg_pattern_1
      return unless @any_show
      @bg_pattern |= @chr_mem[@io_addr & 0x1fff] * 0x100
    end

    def scroll_clock_x
      return unless @any_show
      if @scroll_addr_0_4 < 0x001f
        @scroll_addr_0_4 += 1
        @name_io_addr += 1 # make cache consistent
      else
        @scroll_addr_0_4 = 0
        @scroll_addr_5_14 ^= 0x0400
        @name_io_addr ^= 0x041f # make cache consistent
      end
    end

    def scroll_reset_x
      return unless @any_show
      @scroll_addr_0_4 = @scroll_latch & 0x001f
      @scroll_addr_5_14 = (@scroll_addr_5_14 & 0x7be0) | (@scroll_latch & 0x0400)
      @name_io_addr = (@scroll_addr_0_4 | @scroll_addr_5_14) & 0x0fff | 0x2000 # make cache consistent
    end

    def scroll_clock_y
      return unless @any_show
      if @scroll_addr_5_14 & 0x7000 != 0x7000
        @scroll_addr_5_14 += 0x1000
      else
        mask = @scroll_addr_5_14 & 0x03e0
        if mask == 0x03a0
          @scroll_addr_5_14 ^= 0x0800
          @scroll_addr_5_14 &= 0x0c00
        elsif mask == 0x03e0
          @scroll_addr_5_14 &= 0x0c00
        else
          @scroll_addr_5_14 = (@scroll_addr_5_14 & 0x0fe0) + 32
        end
      end

      @name_io_addr = (@scroll_addr_0_4 | @scroll_addr_5_14) & 0x0fff | 0x2000 # make cache consistent
    end

    def preload_tiles
      return unless @any_show
      @bg_pixels[@scroll_xfine, 8] = @bg_pattern_lut[@bg_pattern]
    end

    def load_tiles
      return unless @any_show
      @bg_pixels.rotate!(8)
      @bg_pixels[@scroll_xfine, 8] = @bg_pattern_lut[@bg_pattern]
    end

    def evaluate_sprites_even
      return unless @any_show
      @sp_latch = @sp_ram[@sp_addr]
    end

    def evaluate_sprites_odd
      return unless @any_show

      # we first check phase 1 since it is the most-likely case
      if @sp_phase # nil represents phase 1
        # the second most-likely case is phase 9
        if @sp_phase == 9
          evaluate_sprites_odd_phase_9
        else
          # other cases are relatively rare
          case @sp_phase
          # when 1 then evaluate_sprites_odd_phase_1
          # when 9 then evaluate_sprites_odd_phase_9
          when 2 then evaluate_sprites_odd_phase_2
          when 3 then evaluate_sprites_odd_phase_3
          when 4 then evaluate_sprites_odd_phase_4
          when 5 then evaluate_sprites_odd_phase_5
          when 6 then evaluate_sprites_odd_phase_6
          when 7 then evaluate_sprites_odd_phase_7
          when 8 then evaluate_sprites_odd_phase_8
          end
        end
      else
        evaluate_sprites_odd_phase_1
      end
    end

    def evaluate_sprites_odd_phase_1
      @sp_index += 1
      if @sp_latch <= @scanline && @scanline < @sp_latch + @sp_height
        @sp_addr += 1
        @sp_phase = 2
        @sp_buffer[@sp_buffered] = @sp_latch
      elsif @sp_index == 64
        @sp_addr = 0
        @sp_phase = 9
      elsif @sp_index == 2
        @sp_addr = 8
      else
        @sp_addr += 4
      end
    end

    def evaluate_sprites_odd_phase_2
      @sp_addr += 1
      @sp_phase = 3
      @sp_buffer[@sp_buffered + 1] = @sp_latch
    end

    def evaluate_sprites_odd_phase_3
      @sp_addr += 1
      @sp_phase = 4
      @sp_buffer[@sp_buffered + 2] = @sp_latch
    end

    def evaluate_sprites_odd_phase_4
      @sp_buffer[@sp_buffered + 3] = @sp_latch
      @sp_buffered += 4
      if @sp_index != 64
        @sp_phase = @sp_buffered != @sp_limit ? nil : 5
        if @sp_index != 2
          @sp_addr += 1
          @sp_zero_in_line ||= @sp_index == 1
        else
          @sp_addr = 8
        end
      else
        @sp_addr = 0
        @sp_phase = 9
      end
    end

    def evaluate_sprites_odd_phase_5
      if @sp_latch <= @scanline && @scanline < @sp_latch + @sp_height
        @sp_phase = 6
        @sp_addr = (@sp_addr + 1) & 0xff
        @sp_overflow = true
      else
        @sp_addr = ((@sp_addr + 4) & 0xfc) + ((@sp_addr + 1) & 3)
        if @sp_addr <= 5
          @sp_phase = 9
          @sp_addr &= 0xfc
        end
      end
    end

    def evaluate_sprites_odd_phase_6
      @sp_phase = 7
      @sp_addr = (@sp_addr + 1) & 0xff
    end

    def evaluate_sprites_odd_phase_7
      @sp_phase = 8
      @sp_addr = (@sp_addr + 1) & 0xff
    end

    def evaluate_sprites_odd_phase_8
      @sp_phase = 9
      @sp_addr = (@sp_addr + 1) & 0xff
      @sp_addr += 1 if @sp_addr & 3 == 3
      @sp_addr &= 0xfc
    end

    def evaluate_sprites_odd_phase_9
      @sp_addr = (@sp_addr + 4) & 0xff
    end

    def load_extended_sprites
      return unless @any_show
      if 32 < @sp_buffered
        buffer_idx = 32
        begin
          addr = open_sprite(buffer_idx)
          pat0 = @chr_mem[addr]
          pat1 = @chr_mem[addr | 8]
          load_sprite(pat0, pat1, buffer_idx) if pat0 != 0 || pat1 != 0
          buffer_idx += 4
        end while buffer_idx != @sp_buffered
      end
    end

    def render_pixel
      if @any_show
        pixel = @bg_enabled ? @bg_pixels[@hclk % 8] : 0
        if @sp_active && (sprite = @sp_map[@hclk])
          if pixel % 4 == 0
            pixel = sprite[2]
          else
            @sp_zero_hit = true if sprite[1] && @hclk != 255
            pixel = sprite[2] unless sprite[0]
          end
        end
      else
        pixel = @scroll_addr_5_14 & 0x3f00 == 0x3f00 ? @scroll_addr_0_4 : 0
        @bg_pixels[@hclk % 8] = 0
      end
      @output_pixels << @output_color[pixel]
    end

    # just a placeholder; used for batch_render_pixels optimization
    def batch_render_eight_pixels
    end

    def boot
      @vblank = true
      @hclk = HCLOCK_DUMMY
      @hclk_target = FOREVER_CLOCK
    end

    def vblank_0
      @vblanking = true
      @hclk = HCLOCK_VBLANK_1
    end

    def vblank_1
      @vblank ||= @vblanking
      @vblanking = false
      @sp_visible = false
      @sp_active = false
      @hclk = HCLOCK_VBLANK_2
    end

    def vblank_2
      @vblank ||= @vblanking
      @vblanking = false
      @hclk = HCLOCK_DUMMY
      @hclk_target = FOREVER_CLOCK
      @cpu.do_nmi(@cpu.next_frame_clock) if @need_nmi && @vblank
    end

    def update_enabled_flags
      return unless @any_show
      @bg_enabled = @bg_show
      @sp_enabled = @sp_show
      @sp_active = @sp_enabled && @sp_visible
    end

    def update_enabled_flags_edge
      @bg_enabled = @bg_show_edge
      @sp_enabled = @sp_show_edge
      @sp_active = @sp_enabled && @sp_visible
    end

    ###########################################################################
    # default core

    def debug_logging(scanline, hclk, hclk_target)
      hclk = "forever" if hclk == FOREVER_CLOCK
      hclk_target = "forever" if hclk_target == FOREVER_CLOCK

      @conf.debug("ppu: scanline #{ scanline }, hclk #{ hclk }->#{ hclk_target }")
    end

    def run
      @fiber ||= Fiber.new { main_loop }

      debug_logging(@scanline, @hclk, @hclk_target) if @conf.loglevel >= 3

      make_sure_invariants

      @hclk_target = (@vclk + @hclk) * RP2C02_CC unless @fiber.resume
    end

    def wait_frame
      Fiber.yield true
    end

    def wait_zero_clocks
      Fiber.yield if @hclk_target <= @hclk
    end

    def wait_one_clock
      @hclk += 1
      Fiber.yield if @hclk_target <= @hclk
    end

    def wait_two_clocks
      @hclk += 2
      Fiber.yield if @hclk_target <= @hclk
    end

    ### main-loop structure
    #
    # # wait for boot
    # clk_685
    #
    # loop do
    #   # pre-render scanline
    #   clk_341, clk_342, ..., clk_659
    #   while true
    #     # visible scanline (not shown)
    #     clk_320, clk_321, ..., clk_337
    #
    #     # increment scanline
    #     clk_338
    #     break if @scanline == 240
    #
    #     # visible scanline (shown)
    #     clk_0, clk_1, ..., clk_319
    #   end
    #
    #   # post-render sacnline (vblank)
    #   do_681,682,684
    # end
    #
    # This method definition also serves as a template for OptimizedCodeBuilder.
    # Comments like "when NNN" are markers for the purpose.
    #
    # rubocop:disable Metrics/MethodLength, Metrics/CyclomaticComplexity, Metrics/PerceivedComplexity, Metrics/AbcSize
    def main_loop
      # when 685

      # wait for boot
      boot
      wait_frame

      while true
        # pre-render scanline

        341.step(589, 8) do
          # when 341, 349, ..., 589
          if @hclk == 341
            @sp_overflow = @sp_zero_hit = @vblanking = @vblank = false
            @scanline = SCANLINE_HDUMMY
          end
          open_name
          wait_two_clocks

          # when 343, 351, ..., 591
          open_attr
          wait_two_clocks

          # when 345, 353, ..., 593
          open_pattern(@bg_pattern_base)
          wait_two_clocks

          # when 347, 355, ..., 595
          open_pattern(@io_addr | 8)
          wait_two_clocks
        end

        597.step(653, 8) do
          # when 597, 605, ..., 653
          if @any_show
            if @hclk == 645
              @scroll_addr_0_4  = @scroll_latch & 0x001f
              @scroll_addr_5_14 = @scroll_latch & 0x7fe0
              @name_io_addr = (@scroll_addr_0_4 | @scroll_addr_5_14) & 0x0fff | 0x2000 # make cache consistent
            end
          end
          open_name
          wait_two_clocks

          # when 599, 607, ..., 655
          # Nestopia uses open_name here?
          open_attr
          wait_two_clocks

          # when 601, 609, ..., 657
          open_pattern(@pattern_end)
          wait_two_clocks

          # when 603, 611, ..., 659
          open_pattern(@io_addr | 8)
          if @hclk == 659
            @hclk = 320
            @vclk += HCLOCK_DUMMY
            @hclk_target -= HCLOCK_DUMMY
          else
            wait_two_clocks
          end
          wait_zero_clocks
        end

        while true
          # visible scanline (not shown)

          # when 320
          load_extended_sprites
          open_name
          @sp_latch = @sp_ram[0] if @any_show
          @sp_buffered = 0
          @sp_zero_in_line = false
          @sp_index = 0
          @sp_phase = 0
          wait_one_clock

          # when 321
          fetch_name
          wait_one_clock

          # when 322
          open_attr
          wait_one_clock

          # when 323
          fetch_attr
          scroll_clock_x
          wait_one_clock

          # when 324
          open_pattern(@io_pattern)
          wait_one_clock

          # when 325
          fetch_bg_pattern_0
          wait_one_clock

          # when 326
          open_pattern(@io_pattern | 8)
          wait_one_clock

          # when 327
          fetch_bg_pattern_1
          wait_one_clock

          # when 328
          preload_tiles
          open_name
          wait_one_clock

          # when 329
          fetch_name
          wait_one_clock

          # when 330
          open_attr
          wait_one_clock

          # when 331
          fetch_attr
          scroll_clock_x
          wait_one_clock

          # when 332
          open_pattern(@io_pattern)
          wait_one_clock

          # when 333
          fetch_bg_pattern_0
          wait_one_clock

          # when 334
          open_pattern(@io_pattern | 8)
          wait_one_clock

          # when 335
          fetch_bg_pattern_1
          wait_one_clock

          # when 336
          open_name
          wait_one_clock

          # when 337
          if @any_show
            update_enabled_flags_edge
            @cpu.next_frame_clock = RP2C02_HVSYNC_1 if @scanline == SCANLINE_HDUMMY && @odd_frame
          end
          wait_one_clock

          # when 338
          open_name
          @scanline += 1
          if @scanline != SCANLINE_VBLANK
            if @any_show
              line = @scanline != 0 || !@odd_frame ? 341 : 340
            else
              update_enabled_flags_edge
              line = 341
            end
            @hclk = 0
            @vclk += line
            @hclk_target = @hclk_target <= line ? 0 : @hclk_target - line
          else
            @hclk = HCLOCK_VBLANK_0
            wait_zero_clocks
            break
          end
          wait_zero_clocks

          # visible scanline (shown)
          0.step(248, 8) do
            # when 0, 8, ..., 248
            if @any_show
              if @hclk == 64
                @sp_addr = @regs_oam & 0xf8 # SP_OFFSET_TO_0_1
                @sp_phase = nil
                @sp_latch = 0xff
              end
              load_tiles
              batch_render_eight_pixels
              evaluate_sprites_even if @hclk >= 64
              open_name
            end
            render_pixel
            wait_one_clock

            # when 1, 9, ..., 249
            if @any_show
              fetch_name
              evaluate_sprites_odd if @hclk >= 64
            end
            render_pixel
            wait_one_clock

            # when 2, 10, ..., 250
            if @any_show
              evaluate_sprites_even if @hclk >= 64
              open_attr
            end
            render_pixel
            wait_one_clock

            # when 3, 11, ..., 251
            if @any_show
              fetch_attr
              evaluate_sprites_odd if @hclk >= 64
              scroll_clock_y if @hclk == 251
              scroll_clock_x
            end
            render_pixel
            wait_one_clock

            # when 4, 12, ..., 252
            if @any_show
              evaluate_sprites_even if @hclk >= 64
              open_pattern(@io_pattern)
            end
            render_pixel
            wait_one_clock

            # when 5, 13, ..., 253
            if @any_show
              fetch_bg_pattern_0
              evaluate_sprites_odd if @hclk >= 64
            end
            render_pixel
            wait_one_clock

            # when 6, 14, ..., 254
            if @any_show
              evaluate_sprites_even if @hclk >= 64
              open_pattern(@io_pattern | 8)
            end
            render_pixel
            wait_one_clock

            # when 7, 15, ..., 255
            if @any_show
              fetch_bg_pattern_1
              evaluate_sprites_odd if @hclk >= 64
            end
            render_pixel
            # rubocop:disable Style/NestedModifier
            update_enabled_flags if @hclk != 255 if @any_show
            # rubocop:enable Style/NestedModifier
            wait_one_clock
          end

          256.step(312, 8) do
            if @hclk == 256
              # when 256
              open_name
              @sp_latch = 0xff if @any_show
              wait_one_clock

              # when 257
              scroll_reset_x
              @sp_visible = false
              @sp_active = false
              wait_one_clock
            else
              # when 264, 272, ..., 312
              open_name
              wait_two_clocks
            end

            # when 258, 266, ..., 314
            # Nestopia uses open_name here?
            open_attr
            wait_two_clocks

            # when 260, 268, ..., 316
            if @any_show
              buffer_idx = (@hclk - 260) / 2
              open_pattern(buffer_idx >= @sp_buffered ? @pattern_end : open_sprite(buffer_idx))
              # rubocop:disable Style/NestedModifier
              @regs_oam = 0 if @scanline == 238 if @hclk == 316
              # rubocop:enable Style/NestedModifier
            end
            wait_one_clock

            # when 261, 269, ..., 317
            if @any_show
              @io_pattern = @chr_mem[@io_addr & 0x1fff] if (@hclk - 261) / 2 < @sp_buffered
            end
            wait_one_clock

            # when 262, 270, ..., 318
            open_pattern(@io_addr | 8)
            wait_one_clock

            # when 263, 271, ..., 319
            if @any_show
              buffer_idx = (@hclk - 263) / 2
              if buffer_idx < @sp_buffered
                pat0 = @io_pattern
                pat1 = @chr_mem[@io_addr & 0x1fff]
                load_sprite(pat0, pat1, buffer_idx) if pat0 != 0 || pat1 != 0
              end
            end
            wait_one_clock
          end
        end

        # post-render scanline (vblank)

        # when 681
        vblank_0
        wait_zero_clocks

        # when 682
        vblank_1
        wait_zero_clocks

        # when 684
        vblank_2
        wait_frame
      end
    end
    # rubocop:enable Metrics/MethodLength, Metrics/CyclomaticComplexity, Metrics/PerceivedComplexity, Metrics/AbcSize

    ###########################################################################
    # optimized core generator
    class OptimizedCodeBuilder
      include CodeOptimizationHelper

      OPTIONS = [
        :method_inlining, :ivar_localization,
        :split_show_mode, :split_a12_checks, :clock_specialization,
        :fastpath, :batch_render_pixels,
      ]

      def build
        depends(:ivar_localization, :method_inlining)
        depends(:batch_render_pixels, :fastpath)

        mdefs = parse_method_definitions(__FILE__)
        handlers = parse_clock_handlers(mdefs[:main_loop].body)

        handlers = specialize_clock_handlers(handlers) if @clock_specialization
        if @fastpath
          handlers = add_fastpath(handlers) do |fastpath, hclk|
            @batch_render_pixels ? batch_render_pixels(fastpath, hclk) : fastpath
          end
        end
        code = build_loop(handlers)
        code = ppu_expand_methods(code, mdefs) if @method_inlining

        if @split_show_mode
          code, code_no_show = split_mode(code, "@any_show")
          if @split_a12_checks
            code, code_no_a12 = split_mode(code, "@a12_monitor")
            code = branch("@a12_monitor", code, code_no_a12)
          end
          code = branch("@any_show", code, code_no_show)
        end

        code = gen(
          mdefs[:make_sure_invariants].body,
          code,
          "@hclk_target = (@vclk + @hclk) * RP2C02_CC"
        )

        code = localize_instance_variables(code) if @ivar_localization

        gen(
          "def self.run",
          *(@loglevel >= 3 ? ["  debug_logging(@scanline, @hclk, @hclk_target)"] : []),
          indent(2, code),
          "end",
        )
      end

      COMMANDS = {
        wait_zero_clocks: "",
        wait_one_clock:   "@hclk += 1\n",
        wait_two_clocks:  "@hclk += 2\n",
        wait_frame:       "return\n",
      }

      # extracts the actions for each clock from CPU#main_loop
      def parse_clock_handlers(main_loop)
        handlers = {}
        main_loop.scan(/^( *)# when (.*)\n((?:\1.*\n|\n)*?\1wait_.*\n)/) do |indent, hclks, body|
          body = indent(-indent.size, body)
          body = body.gsub(/^( *)break\n/, "")
          body = expand_methods(body, COMMANDS)
          if hclks =~ /^(\d+), (\d+), \.\.\., (\d+)$/
            first, second, last = $1.to_i, $2.to_i, $3.to_i
            first.step(last, second - first) do |hclk|
              handlers[hclk] = body
            end
          else
            handlers[hclks.to_i] = body
          end
        end
        handlers
      end

      # split clock handlers that contains a branch depending on clock
      def specialize_clock_handlers(handlers)
        handlers.each do |hclk, handler|
          # pre-caluculate some conditions like `@hclk == 64` with `false`
          handler = handler.gsub(/@hclk (==|>=|!=) (\d+)/) { hclk.send($1.to_sym, $2.to_i) }

          # remove disabled branches like `if false ... end`
          handlers[hclk] = remove_trivial_branches(handler)
        end
      end

      # pass a fastpath
      def add_fastpath(handlers)
        handlers.each do |hclk, handler|
          next unless hclk % 8 == 0 && hclk < 256
          fastpath = gen(*(0..7).map {|i| handlers[hclk + i] })
          fastpath = yield fastpath, hclk
          handlers[hclk] = branch("@hclk + 8 <= @hclk_target", fastpath, handler)
        end
      end

      # replace eight `render_pixel` calls with one optimized batch version
      def batch_render_pixels(fastpath, hclk)
        fastpath = expand_methods(fastpath, render_pixel: gen(
          "unless @any_show",
          "  @bg_pixels[@hclk % 8] = 0",
          "  @output_pixels << @output_color[@scroll_addr_5_14 & 0x3f00 == 0x3f00 ? @scroll_addr_0_4 : 0]",
          "end",
        ))
        expand_methods(fastpath, batch_render_eight_pixels: gen(
          "# batch-version of render_pixel",
          "if @any_show",
          "  if @sp_active",
          "    if @bg_enabled",
          *(0..7).flat_map do |i|
            [
              "      pixel#{ i } = @bg_pixels[#{ i }]",
              "      if sprite = @sp_map[@hclk#{ i != 0 ? " + #{ i }" : "" }]",
              "        if pixel#{ i } % 4 == 0",
              "          pixel#{ i } = sprite[2]",
              "        else",
              *(hclk + i == 255 ? [] : ["          @sp_zero_hit = true if sprite[1]"]),
              "          pixel#{ i } = sprite[2] unless sprite[0]",
              "        end",
              "      end",
            ]
          end,
          "      @output_pixels << " + (0..7).map {|n| "@output_color[pixel#{ n }]" } * " << ",
          "    else",
          *(0..7).map do |i|
            "      pixel#{ i } = (sprite = @sp_map[@hclk #{ i != 0 ? " + #{ i }" : "" }]) ? sprite[2] : 0"
          end,
          "      @output_pixels << " + (0..7).map {|n| "@output_color[pixel#{ n }]" } * " << ",
          "    end",
          "  else",
          "    if @bg_enabled # this is the true hot-spot",
          "      @output_pixels << " + (0..7).map {|n| "@output_color[@bg_pixels[#{ n }]]" } * " << ",
          "    else",
          "      clr = @output_color[0]",
          "      @output_pixels << " + ["clr"] * 8 * " << ",
          "    end",
          "  end",
          "end",
        ))
      end

      # inline method calls
      def ppu_expand_methods(code, mdefs)
        code = expand_inline_methods(code, :open_sprite, mdefs[:open_sprite])

        # twice is enough
        expand_methods(expand_methods(code, mdefs), mdefs)
      end

      # create two version of the same code by evaluating easy branches
      # CAUTION: the condition must be invariant during PPU#run
      def split_mode(code, cond)
        %w(true false).map do |bool|
          rebuild_loop(remove_trivial_branches(replace_cond_var(code, cond, bool)))
        end
      end

      # generate a main code
      def build_loop(handlers)
        clauses = {}
        handlers.sort.each do |hclk, handler|
          (clauses[handler] ||= []) << hclk
        end

        gen(
          "while @hclk_target > @hclk",
          "  case @hclk",
          *clauses.invert.sort.map do |hclks, handler|
            "  when #{ hclks * ", " }\n" + indent(4, handler)
          end,
          "  end",
          "end",
        )
      end

      # deconstruct a loop, unify handlers, and re-generate a new loop
      def rebuild_loop(code)
        handlers = {}
        code.scan(/^  when ((?:\d+, )*\d+)\n((?:    .*\n|\n)*)/) do |hclks, handler|
          hclks.split(", ").each do |hclk|
            handlers[hclk.to_i] = indent(-4, handler)
          end
        end
        build_loop(handlers)
      end
    end
  end
end
