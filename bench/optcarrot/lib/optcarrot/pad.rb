module Optcarrot
  # Pad pair implementation (NES has two built-in game pad.)
  class Pads
    def inspect
      "#<#{ self.class }>"
    end

    ###########################################################################
    # initialization

    def initialize(conf, cpu, apu)
      @conf = conf
      @cpu = cpu
      @apu = apu
      @pads = [Pad.new, Pad.new]
    end

    def reset
      @cpu.add_mappings(0x4016, method(:peek_401x), method(:poke_4016))
      @cpu.add_mappings(0x4017, method(:peek_401x), @apu.method(:poke_4017)) # delegate 4017H to APU
      @pads[0].reset
      @pads[1].reset
    end

    def peek_401x(addr)
      @cpu.update
      @pads[addr - 0x4016].peek | 0x40
    end

    def poke_4016(_addr, data)
      @pads[0].poke(data)
      @pads[1].poke(data)
    end

    ###########################################################################
    # APIs

    def keydown(pad, btn)
      @pads[pad].buttons |= 1 << btn
    end

    def keyup(pad, btn)
      @pads[pad].buttons &= ~(1 << btn)
    end
  end

  ###########################################################################
  # each pad
  class Pad
    A      = 0
    B      = 1
    SELECT = 2
    START  = 3
    UP     = 4
    DOWN   = 5
    LEFT   = 6
    RIGHT  = 7

    def initialize
      reset
    end

    def reset
      @strobe = false
      @buttons = @stream = 0
    end

    def poke(data)
      prev = @strobe
      @strobe = data[0] == 1
      @stream = ((poll_state << 1) ^ -512) if prev && !@strobe
    end

    def peek
      return poll_state & 1 if @strobe
      @stream >>= 1
      return @stream[0]
    end

    def poll_state
      state = @buttons

      # prohibit impossible simultaneous keydown (right and left, up and down)
      state &= 0b11001111 if state & 0b00110000 == 0b00110000
      state &= 0b00111111 if state & 0b11000000 == 0b11000000

      state
    end

    attr_accessor :buttons
  end
end
