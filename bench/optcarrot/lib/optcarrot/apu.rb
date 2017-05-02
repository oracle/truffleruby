module Optcarrot
  # APU implementation (audio output)
  class APU
    CLK_M2_MUL   = 6
    CLK_NTSC     = 39_375_000 * CLK_M2_MUL
    CLK_NTSC_DIV = 11

    CHANNEL_OUTPUT_MUL   = 256
    CHANNEL_OUTPUT_DECAY = CHANNEL_OUTPUT_MUL / 4 - 1

    FRAME_CLOCKS = [29830, 1, 1, 29828].map {|n| RP2A03_CC * n }
    OSCILLATOR_CLOCKS = [
      [7458, 7456, 7458, 7458],
      [7458, 7456, 7458, 7458 + 7452]
    ].map {|a| a.map {|n| RP2A03_CC * n } }

    def inspect
      "#<#{ self.class }>"
    end

    ###########################################################################
    # initialization

    def initialize(conf, cpu, rate, bits)
      @conf = conf
      @cpu = cpu

      @pulse_0, @pulse_1 = Pulse.new(self), Pulse.new(self)
      @triangle = Triangle.new(self)
      @noise = Noise.new(self)
      @dmc = DMC.new(@cpu, self)
      @mixer = Mixer.new(@pulse_0, @pulse_1, @triangle, @noise, @dmc)

      @conf.fatal("audio sample rate must be >= 11050") if rate < 11050
      @conf.fatal("audio bit depth must be 8 or 16") if bits != 8 && bits != 16

      @settings_rate = rate

      @output = []
      @buffer = []

      @fixed_clock = 1
      @rate_clock = 1
      @rate_counter = 0
      @frame_counter = 0
      @frame_divider = 0
      @frame_irq_clock = 0
      @frame_irq_repeat = 0
      @dmc_clock = 0

      reset(false)
    end

    def reset_mapping
      @frame_counter /= @fixed_clock
      @rate_counter /= @fixed_clock
      multiplier = 0
      while true
        multiplier += 1
        break if multiplier >= 512
        break if CLK_NTSC * multiplier % @settings_rate == 0
      end
      @rate_clock = CLK_NTSC * multiplier / @settings_rate
      @fixed_clock = CLK_NTSC_DIV * multiplier
      @frame_counter *= @fixed_clock
      @rate_counter *= @fixed_clock

      @mixer.reset
      @buffer.clear

      multiplier = 0
      while true
        multiplier += 1
        break if multiplier >= 0x1000
        break if CLK_NTSC * (multiplier + 1) / @settings_rate > 0x7ffff
        break if CLK_NTSC * multiplier % @settings_rate == 0
      end
      rate = CLK_NTSC * multiplier / @settings_rate
      fixed = CLK_NTSC_DIV * CPU::CLK_1 * multiplier

      @pulse_0 .update_settings(rate, fixed)
      @pulse_1 .update_settings(rate, fixed)
      @triangle.update_settings(rate, fixed)
      @noise   .update_settings(rate, fixed)

      @cpu.add_mappings(0x4000, method(:peek_40xx), @pulse_0 .method(:poke_0))
      @cpu.add_mappings(0x4001, method(:peek_40xx), @pulse_0 .method(:poke_1))
      @cpu.add_mappings(0x4002, method(:peek_40xx), @pulse_0 .method(:poke_2))
      @cpu.add_mappings(0x4003, method(:peek_40xx), @pulse_0 .method(:poke_3))
      @cpu.add_mappings(0x4004, method(:peek_40xx), @pulse_1 .method(:poke_0))
      @cpu.add_mappings(0x4005, method(:peek_40xx), @pulse_1 .method(:poke_1))
      @cpu.add_mappings(0x4006, method(:peek_40xx), @pulse_1 .method(:poke_2))
      @cpu.add_mappings(0x4007, method(:peek_40xx), @pulse_1 .method(:poke_3))
      @cpu.add_mappings(0x4008, method(:peek_40xx), @triangle.method(:poke_0))
      @cpu.add_mappings(0x400a, method(:peek_40xx), @triangle.method(:poke_2))
      @cpu.add_mappings(0x400b, method(:peek_40xx), @triangle.method(:poke_3))
      @cpu.add_mappings(0x400c, method(:peek_40xx), @noise   .method(:poke_0))
      @cpu.add_mappings(0x400e, method(:peek_40xx), @noise   .method(:poke_2))
      @cpu.add_mappings(0x400f, method(:peek_40xx), @noise   .method(:poke_3))
      @cpu.add_mappings(0x4010, method(:peek_40xx), @dmc     .method(:poke_0))
      @cpu.add_mappings(0x4011, method(:peek_40xx), @dmc     .method(:poke_1))
      @cpu.add_mappings(0x4012, method(:peek_40xx), @dmc     .method(:poke_2))
      @cpu.add_mappings(0x4013, method(:peek_40xx), @dmc     .method(:poke_3))
      @cpu.add_mappings(0x4015, method(:peek_4015), method(:poke_4015))
      @frame_irq_clock = (@frame_counter / @fixed_clock) - CPU::CLK_1
    end

    def reset(mapping = true)
      @cycles_ratecounter = 0
      @frame_divider = 0
      @frame_irq_clock = FOREVER_CLOCK
      @frame_irq_repeat = 0
      @dmc_clock = DMC::LUT[0]
      @frame_counter = FRAME_CLOCKS[0] * @fixed_clock

      reset_mapping if mapping

      @pulse_0.reset
      @pulse_1.reset
      @triangle.reset
      @noise.reset
      @dmc.reset
      @mixer.reset
      @buffer.clear
      @oscillator_clocks = OSCILLATOR_CLOCKS[0]
    end

    ###########################################################################
    # other APIs

    attr_reader :output

    def do_clock
      clock_dma(@cpu.current_clock)
      clock_frame_irq(@cpu.current_clock) if @frame_irq_clock <= @cpu.current_clock
      @dmc_clock < @frame_irq_clock ? @dmc_clock : @frame_irq_clock
    end

    def clock_dma(clk)
      clock_dmc(clk) if @dmc_clock <= clk
    end

    def update(target = @cpu.update)
      target *= @fixed_clock
      proceed(target)
      clock_frame_counter if @frame_counter < target
    end

    def update_latency
      update(@cpu.update + 1)
    end

    def update_delta
      elapsed = @cpu.update
      delta = @frame_counter != elapsed * @fixed_clock
      update(elapsed + 1)
      delta
    end

    def vsync
      flush_sound
      update(@cpu.current_clock)
      frame = @cpu.next_frame_clock
      @dmc_clock -= frame
      @frame_irq_clock -= frame if @frame_irq_clock != FOREVER_CLOCK
      frame *= @fixed_clock
      @rate_counter -= frame
      @frame_counter -= frame
    end

    ###########################################################################
    # helpers

    def clock_oscillators(two_clocks)
      @pulse_0.clock_envelope
      @pulse_1.clock_envelope
      @triangle.clock_linear_counter
      @noise.clock_envelope
      return unless two_clocks
      @pulse_0.clock_sweep(-1)
      @pulse_1.clock_sweep(0)
      @triangle.clock_length_counter
      @noise.clock_length_counter
    end

    def clock_dmc(target)
      begin
        if @dmc.clock_dac
          update(@dmc_clock)
          @dmc.update
        end
        @dmc_clock += @dmc.freq
        @dmc.clock_dma
      end while @dmc_clock <= target
    end

    def clock_frame_counter
      clock_oscillators(@frame_divider[0] == 1)
      @frame_divider = (@frame_divider + 1) & 3
      @frame_counter += @oscillator_clocks[@frame_divider] * @fixed_clock
    end

    def clock_frame_irq(target)
      @cpu.do_irq(CPU::IRQ_FRAME, @frame_irq_clock)
      begin
        @frame_irq_clock += FRAME_CLOCKS[1 + @frame_irq_repeat % 3]
        @frame_irq_repeat += 1
      end while @frame_irq_clock <= target
    end

    def flush_sound
      if @buffer.size < @settings_rate / 60
        target = @cpu.current_clock * @fixed_clock
        proceed(target)
        if @buffer.size < @settings_rate / 60
          clock_frame_counter if @frame_counter < target
          @buffer << @mixer.sample while @buffer.size < @settings_rate / 60
        end
      end
      @output.clear
      @output.concat(@buffer) # Array#replace creates an object internally
      @buffer.clear
    end

    def proceed(target)
      while @rate_counter < target && @buffer.size < @settings_rate / 60
        @buffer << @mixer.sample
        clock_frame_counter if @frame_counter <= @rate_counter
        @rate_counter += @rate_clock
      end
    end

    ###########################################################################
    # mapped memory handlers

    # Control
    def poke_4015(_addr, data)
      update
      @pulse_0 .enable(data[0] == 1)
      @pulse_1 .enable(data[1] == 1)
      @triangle.enable(data[2] == 1)
      @noise   .enable(data[3] == 1)
      @dmc     .enable(data[4] == 1)
    end

    # Status
    def peek_4015(_addr)
      elapsed = @cpu.update
      clock_frame_irq(elapsed) if @frame_irq_clock <= elapsed
      update(elapsed) if @frame_counter < elapsed * @fixed_clock
      @cpu.clear_irq(CPU::IRQ_FRAME) |
        (@pulse_0 .status ? 0x01 : 0) |
        (@pulse_1 .status ? 0x02 : 0) |
        (@triangle.status ? 0x04 : 0) |
        (@noise   .status ? 0x08 : 0) |
        (@dmc     .status ? 0x10 : 0)
    end

    # Frame counter (NOTE: this handler is called via Pads)
    def poke_4017(_addr, data)
      n = @cpu.update
      n += CPU::CLK_1 if @cpu.odd_clock?
      update(n)
      clock_frame_irq(n) if @frame_irq_clock <= n
      n += CPU::CLK_1
      @oscillator_clocks = OSCILLATOR_CLOCKS[data[7]]
      @frame_counter = (n + @oscillator_clocks[0]) * @fixed_clock
      @frame_divider = 0
      @frame_irq_clock = data & 0xc0 != 0 ? FOREVER_CLOCK : n + FRAME_CLOCKS[0]
      @frame_irq_repeat = 0
      @cpu.clear_irq(CPU::IRQ_FRAME) if data[6] != 0
      clock_oscillators(true) if data[7] != 0
    end

    def peek_40xx(_addr)
      0x40
    end

    ###########################################################################
    # helper classes

    # A counter for note length
    class LengthCounter
      LUT = [
        0x0a, 0xfe, 0x14, 0x02, 0x28, 0x04, 0x50, 0x06, 0xa0, 0x08, 0x3c, 0x0a, 0x0e, 0x0c, 0x1a, 0x0e,
        0x0c, 0x10, 0x18, 0x12, 0x30, 0x14, 0x60, 0x16, 0xc0, 0x18, 0x48, 0x1a, 0x10, 0x1c, 0x20, 0x1e,
      ]
      def reset
        @enabled = false
        @count = 0
      end

      attr_reader :count

      def enable(enabled)
        @enabled = enabled
        @count = 0 unless @enabled
        @enabled
      end

      def write(data, frame_counter_delta)
        @count = @enabled ? LUT[data] : 0 if frame_counter_delta || @count == 0
      end

      def clock
        return false if @count == 0
        @count -= 1
        return @count == 0
      end
    end

    # Wave envelope
    class Envelope
      attr_reader :output, :looping

      def reset_clock
        @reset = true
      end

      def reset
        @output = 0
        @count = 0
        @volume_base = @volume = 0
        @constant = true
        @looping = false
        @reset = false
        update_output
      end

      def clock
        if @reset
          @reset = false
          @volume = 0x0f
        else
          if @count != 0
            @count -= 1
            return
          end
          @volume = (@volume - 1) & 0x0f if @volume != 0 || @looping
        end
        @count = @volume_base
        update_output
      end

      def write(data)
        @volume_base = data & 0x0f
        @constant = data[4] == 1
        @looping = data[5] == 1
        update_output
      end

      def update_output
        @output = (@constant ? @volume_base : @volume) * CHANNEL_OUTPUT_MUL
      end
    end

    # Mixer (with DC Blocking filter)
    class Mixer
      VOL   = 192
      P_F   = 900
      P_0   = 9552 * CHANNEL_OUTPUT_MUL * VOL * (P_F / 100)
      P_1   = 8128 * CHANNEL_OUTPUT_MUL * P_F
      P_2   = P_F * 100
      TND_F = 500
      TND_0 = 16367 * CHANNEL_OUTPUT_MUL * VOL * (TND_F / 100)
      TND_1 = 24329 * CHANNEL_OUTPUT_MUL * TND_F
      TND_2 = TND_F * 100

      def initialize(pulse_0, pulse_1, triangle, noise, dmc)
        @pulse_0, @pulse_1, @triangle, @noise, @dmc = pulse_0, pulse_1, triangle, noise, dmc
      end

      def reset
        @acc = @prev = @next = 0
      end

      def sample
        dac0 = @pulse_0.sample + @pulse_1.sample
        dac1 = @triangle.sample + @noise.sample + @dmc.sample
        sample = (P_0 * dac0 / (P_1 + P_2 * dac0)) + (TND_0 * dac1 / (TND_1 + TND_2 * dac1))

        @acc -= @prev
        @prev = sample << 15
        @acc += @prev - @next * 3 # POLE
        sample = @next = @acc >> 15

        sample = -0x7fff if sample < -0x7fff
        sample = 0x7fff if sample > 0x7fff
        sample
      end
    end

    # base class for oscillator channels (Pulse, Triangle, and Noise)
    class Oscillator
      def inspect
        "#<#{ self.class }>"
      end

      def initialize(apu)
        @apu = apu
        @rate = @fixed = 1
        @envelope = @length_counter = @wave_length = nil
      end

      def reset
        @timer = 2048 * @fixed # 2048: reset cycles
        @freq = @fixed
        @amp = 0

        @wave_length = 0 if @wave_length
        @envelope.reset if @envelope
        @length_counter.reset if @length_counter
        @active = active?
      end

      def active?
        return false if @length_counter && @length_counter.count == 0
        return false if @envelope && @envelope.output == 0
        return true
      end

      def poke_0(_addr, data)
        if @envelope
          @apu.update_latency
          @envelope.write(data)
          @active = active?
        end
      end

      def poke_2(_addr, data)
        @apu.update
        if @wave_length
          @wave_length = (@wave_length & 0x0700) | (data & 0x00ff)
          update_freq
        end
      end

      def poke_3(_addr, data)
        delta = @apu.update_delta
        if @wave_length
          @wave_length = (@wave_length & 0x00ff) | ((data & 0x07) << 8)
          update_freq
        end
        @envelope.reset_clock if @envelope
        @length_counter.write(data >> 3, delta) if @length_counter
        @active = active?
      end

      def enable(enabled)
        @length_counter.enable(enabled)
        @active = active?
      end

      def update_settings(r, f)
        @freq = @freq / @fixed * f
        @timer = @timer / @fixed * f
        @rate, @fixed = r, f
      end

      def status
        @length_counter.count > 0
      end

      def clock_envelope
        @envelope.clock
        @active = active?
      end
    end

    #--------------------------------------------------------------------------

    ### Pulse channel ###
    class Pulse < Oscillator
      MIN_FREQ = 0x0008
      MAX_FREQ = 0x07ff
      WAVE_FORM = [0b11111101, 0b11111001, 0b11100001, 0b00000110].map {|n| (0..7).map {|i| n[i] * 0x1f } }

      def initialize(_apu)
        super
        @wave_length = 0
        @envelope = Envelope.new
        @length_counter = LengthCounter.new
      end

      def reset
        super
        @freq = @fixed * 2
        @valid_freq = false
        @step = 0
        @form = WAVE_FORM[0]
        @sweep_rate = 0
        @sweep_count = 1
        @sweep_reload = false
        @sweep_increase = -1
        @sweep_shift = 0
      end

      def active?
        super && @valid_freq
      end

      def update_freq
        if @wave_length >= MIN_FREQ && @wave_length + (@sweep_increase & @wave_length >> @sweep_shift) <= MAX_FREQ
          @freq = (@wave_length + 1) * 2 * @fixed
          @valid_freq = true
        else
          @valid_freq = false
        end
        @active = active?
      end

      def poke_0(_addr, data)
        super
        @form = WAVE_FORM[data >> 6 & 3]
      end

      def poke_1(_addr, data)
        @apu.update
        @sweep_increase = data[3] != 0 ? 0 : -1
        @sweep_shift = data & 0x07
        @sweep_rate = 0
        if data[7] == 1 && @sweep_shift > 0
          @sweep_rate = ((data >> 4) & 0x07) + 1
          @sweep_reload = true
        end
        update_freq
      end

      def poke_3(_addr, _data)
        super
        @step = 0
      end

      def clock_sweep(complement)
        @active = false if !@envelope.looping && @length_counter.clock
        if @sweep_rate != 0
          @sweep_count -= 1
          if @sweep_count == 0
            @sweep_count = @sweep_rate
            if @wave_length >= MIN_FREQ
              shifted = @wave_length >> @sweep_shift
              if @sweep_increase == 0
                @wave_length += complement - shifted
                update_freq
              elsif @wave_length + shifted <= MAX_FREQ
                @wave_length += shifted
                update_freq
              end
            end
          end
        end

        return unless @sweep_reload

        @sweep_reload = false
        @sweep_count = @sweep_rate
      end

      def sample
        sum = @timer
        @timer -= @rate
        if @active
          if @timer < 0
            sum >>= @form[@step]
            begin
              v = -@timer
              v = @freq if v > @freq
              sum += v >> @form[@step = (@step + 1) & 7]
              @timer += @freq
            end while @timer < 0
            @amp = (sum * @envelope.output + @rate / 2) / @rate
          else
            @amp = @envelope.output >> @form[@step]
          end
        else
          if @timer < 0
            count = (-@timer + @freq - 1) / @freq
            @step = (@step + count) & 7
            @timer += count * @freq
          end
          return 0 if @amp < CHANNEL_OUTPUT_DECAY
          @amp -= CHANNEL_OUTPUT_DECAY
        end
        @amp
      end
    end

    #--------------------------------------------------------------------------

    ### Triangle channel ###
    class Triangle < Oscillator
      MIN_FREQ = 2 + 1
      WAVE_FORM = (0..15).to_a + (0..15).to_a.reverse

      def initialize(_apu)
        super
        @wave_length = 0
        @length_counter = LengthCounter.new
      end

      def reset
        super
        @step = 7
        @status = :counting
        @linear_counter_load = 0
        @linear_counter_start = true
        @linear_counter = 0
      end

      def active?
        super && @linear_counter != 0 && @wave_length >= MIN_FREQ
      end

      def update_freq
        @freq = (@wave_length + 1) * @fixed
        @active = active?
      end

      def poke_0(_addr, data)
        super
        @apu.update
        @linear_counter_load = data & 0x7f
        @linear_counter_start = data[7] == 0
      end

      def poke_3(_addr, _data)
        super
        @status = :reload
      end

      def clock_linear_counter
        if @status == :counting
          @linear_counter -= 1 if @linear_counter != 0
        else
          @status = :counting if @linear_counter_start
          @linear_counter = @linear_counter_load
        end
        @active = active?
      end

      def clock_length_counter
        @active = false if @linear_counter_start && @length_counter.clock
      end

      def sample
        if @active
          sum = @timer
          @timer -= @rate
          if @timer < 0
            sum *= WAVE_FORM[@step]
            begin
              v = -@timer
              v = @freq if v > @freq
              sum += v * WAVE_FORM[@step = (@step + 1) & 0x1f]
              @timer += @freq
            end while @timer < 0
            @amp = (sum * CHANNEL_OUTPUT_MUL + @rate / 2) / @rate * 3
          else
            @amp = WAVE_FORM[@step] * CHANNEL_OUTPUT_MUL * 3
          end
        else
          return 0 if @amp < CHANNEL_OUTPUT_DECAY
          @amp -= CHANNEL_OUTPUT_DECAY
          @step = 0
        end
        @amp
      end
    end

    #--------------------------------------------------------------------------

    ### Noise channel ###
    class Noise < Oscillator
      LUT = [4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068]
      NEXT_BITS_1, NEXT_BITS_6 = [1, 6].map do |shifter|
        (0..0x7fff).map {|bits| bits[0] == bits[shifter] ? bits / 2 : bits / 2 + 0x4000 }
      end

      def initialize(_apu)
        super
        @envelope = Envelope.new
        @length_counter = LengthCounter.new
      end

      def reset
        super
        @freq = LUT[0] * @fixed
        @bits = 0x4000
        @shifter = NEXT_BITS_1
      end

      def poke_2(_addr, data)
        @apu.update
        @freq = LUT[data & 0x0f] * @fixed
        @shifter = data[7] != 0 ? NEXT_BITS_6 : NEXT_BITS_1
      end

      def clock_length_counter
        @active = false if !@envelope.looping && @length_counter.clock
      end

      def sample
        @timer -= @rate
        if @active
          return @bits.even? ? @envelope.output * 2 : 0 if @timer >= 0

          sum = @bits.even? ? @timer : 0
          begin
            @bits = @shifter[@bits]
            if @bits.even?
              v = -@timer
              v = @freq if v > @freq
              sum += v
            end
            @timer += @freq
          end while @timer < 0
          return (sum * @envelope.output + @rate / 2) / @rate * 2
        else
          while @timer < 0
            @bits = @shifter[@bits]
            @timer += @freq
          end
          return 0
        end
      end
    end

    #--------------------------------------------------------------------------

    ### DMC channel ###
    class DMC
      LUT = [428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54].map {|n| n * RP2A03_CC }

      def initialize(cpu, apu)
        @apu = apu
        @cpu = cpu
        @freq = LUT[0]
      end

      def reset
        @cur_sample          = 0
        @lin_sample          = 0
        @freq                = LUT[0]
        @loop                = false
        @irq_enable          = false
        @regs_length_counter = 1
        @regs_address        = 0xc000
        @out_active          = false
        @out_shifter         = 0
        @out_dac             = 0
        @out_buffer          = 0x00
        @dma_length_counter  = 0
        @dma_buffered        = false
        @dma_address         = 0xc000
        @dma_buffer          = 0x00
      end

      attr_reader :freq

      def enable(enabled)
        @cpu.clear_irq(CPU::IRQ_DMC)
        if !enabled
          @dma_length_counter = 0
        elsif @dma_length_counter == 0
          @dma_length_counter = @regs_length_counter
          @dma_address = @regs_address
          do_dma unless @dma_buffered
        end
      end

      def sample
        if @cur_sample != @lin_sample
          step = CHANNEL_OUTPUT_MUL * 8
          if @lin_sample + step < @cur_sample
            @lin_sample += step
          elsif @cur_sample < @lin_sample - step
            @lin_sample -= step
          else
            @lin_sample = @cur_sample
          end
        end
        @lin_sample
      end

      def do_dma
        @dma_buffer = @cpu.dmc_dma(@dma_address)
        @dma_address = 0x8000 | ((@dma_address + 1) & 0x7fff)
        @dma_buffered = true
        @dma_length_counter -= 1
        if @dma_length_counter == 0
          if @loop
            @dma_address = @regs_address
            @dma_length_counter = @regs_length_counter
          elsif @irq_enable
            @cpu.do_irq(CPU::IRQ_DMC, @cpu.current_clock)
          end
        end
      end

      def update
        @cur_sample = @out_dac * CHANNEL_OUTPUT_MUL
      end

      def poke_0(_addr, data)
        @loop = data[6] != 0
        @irq_enable = data[7] != 0
        @freq = LUT[data & 0x0f]
        @cpu.clear_irq(CPU::IRQ_DMC) unless @irq_enable
      end

      def poke_1(_addr, data)
        @apu.update
        @out_dac = data & 0x7f
        update
      end

      def poke_2(_addr, data)
        @regs_address = 0xc000 | (data << 6)
      end

      def poke_3(_addr, data)
        @regs_length_counter = (data << 4) + 1
      end

      def clock_dac
        if @out_active
          n = @out_dac + ((@out_buffer & 1) << 2) - 2
          @out_buffer >>= 1
          if 0 <= n && n <= 0x7f && n != @out_dac
            @out_dac = n
            return true
          end
        end
        return false
      end

      def clock_dma
        if @out_shifter == 0
          @out_shifter = 7
          @out_active = @dma_buffered
          if @out_active
            @dma_buffered = false
            @out_buffer = @dma_buffer
            do_dma if @dma_length_counter != 0
          end
        else
          @out_shifter -= 1
        end
      end

      def status
        @dma_length_counter > 0
      end
    end
  end
end
