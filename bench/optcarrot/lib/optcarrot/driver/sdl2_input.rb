require_relative "sdl2"

module Optcarrot
  # Input driver for SDL2
  class SDL2Input < Input
    def init
      SDL2.InitSubSystem(SDL2::INIT_JOYSTICK)
      @event = FFI::MemoryPointer.new(:uint32, 16)

      @keyboard_repeat_offset = SDL2::KeyboardEvent.offset_of(:repeat)
      @keyboard_sym_offset = SDL2::KeyboardEvent.offset_of(:sym)
      @joy_which_offset = SDL2::JoyAxisEvent.offset_of(:which)
      @joyaxis_axis_offset = SDL2::JoyAxisEvent.offset_of(:axis)
      @joyaxis_value_offset = SDL2::JoyAxisEvent.offset_of(:value)
      @joybutton_button_offset = SDL2::JoyButtonEvent.offset_of(:button)

      @joysticks = {}
      SDL2.NumJoysticks.times do |i|
        p SDL2.JoystickNameForIndex(i)
        js = SDL2.JoystickOpen(i)
        @joysticks[SDL2.JoystickInstanceID(js)] = js
        # SDL2.JoystickNumAxes(js)
        # SDL2.JoystickNumButtons(js)
      end

      @key_mapping = DEFAULT_KEY_MAPPING
    end

    def dispose
      @joysticks.each_value do |js|
        SDL2.JoystickClose(js)
      end
      @joysticks.clear
      SDL2.QuitSubSystem(SDL2::INIT_JOYSTICK)
    end

    DEFAULT_KEY_MAPPING = {
      0x20        => [:start, 0],   # space
      0x0d        => [:select, 0],  # return
      0x7a        => [:a, 0],       # `Z'
      0x78        => [:b, 0],       # `X'
      0x4000_004f => [:right, 0],
      0x4000_0050 => [:left, 0],
      0x4000_0051 => [:down, 0],
      0x4000_0052 => [:up, 0],

      # 57 => [:start, 1],   # space
      # 58 => [:select, 1],  # return
      # 25 => [:a, 1],       # `Z'
      # 23 => [:b, 1],       # `X'
      # 72 => [:right, 1],   # right
      # 71 => [:left, 1],    # left
      # 74 => [:down, 1],    # down
      # 73 => [:up, 1],      # up

      0x31 => [:screen_x1, nil],   # `1'
      0x32 => [:screen_x2, nil],   # `2'
      0x33 => [:screen_x3, nil],   # `3'
      0x66 => [:screen_full, nil], # `f'
      0x71 => [:quit, nil],        # `q'
    }

    def joystick_move(axis, value, pads)
      event(pads, value >  0x7000 ? :keydown : :keyup, axis ? :right : :down, 0)
      event(pads, value < -0x7000 ? :keydown : :keyup, axis ? :left : :up, 0)
    end

    def joystick_buttondown(button, pads)
      case button
      when 0 then pads.keydown(0, Pad::A)
      when 1 then pads.keydown(0, Pad::B)
      when 6 then pads.keydown(0, Pad::SELECT)
      when 7 then pads.keydown(0, Pad::START)
      end
    end

    def joystick_buttonup(button, pads)
      case button
      when 0 then pads.keyup(0, Pad::A)
      when 1 then pads.keyup(0, Pad::B)
      when 6 then pads.keyup(0, Pad::SELECT)
      when 7 then pads.keyup(0, Pad::START)
      end
    end

    def tick(_frame, pads)
      while SDL2.PollEvent(@event) != 0
        case @event.read_int

        when 0x300, 0x301 # SDL_KEYDOWN, SDL_KEYUP
          next if @event.get_uint8(@keyboard_repeat_offset) != 0
          key = @key_mapping[@event.get_int(@keyboard_sym_offset)]
          event(pads, @event.read_int == 0x300 ? :keydown : :keyup, *key) if key

        when 0x600 # SDL_JOYAXISMOTION
          which = @event.get_uint32(@joy_which_offset)
          if which == 0 # XXX
            axis = @event.get_uint8(@joyaxis_axis_offset) == 0
            value = @event.get_int16(@joyaxis_value_offset)
            joystick_move(axis, value, pads)
          end

        when 0x603 # SDL_JOYBUTTONDOWN
          which = @event.get_uint32(@joy_which_offset)
          joystick_buttondown(@event.get_uint8(@joybutton_button_offset), pads)

        when 0x604 # SDL_JOYBUTTONUP
          which = @event.get_uint32(@joy_which_offset)
          joystick_buttonup(@event.get_uint8(@joybutton_button_offset), pads)

        when 0x605 # SDL_JOYDEVICEADDED
          which = @event.get_uint32(@joy_which_offset)
          js = SDL2.JoystickOpen(which)
          @joysticks[SDL2.JoystickInstanceID(js)] = js

        when 0x606 # SDL_JOYDEVICEREMOVED
          which = @event.get_uint32(@joy_which_offset)
          @joysticks.delete(which)

        when 0x100 # SDL_QUIT
          exit
        end
      end
    end
  end
end
