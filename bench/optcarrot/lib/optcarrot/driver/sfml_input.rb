require_relative "sfml"

module Optcarrot
  # Input driver for SFML
  class SFMLInput < Input
    def init
      raise "SFMLInput must be used with SFMLVideo" unless @video.is_a?(SFMLVideo)

      @event = FFI::MemoryPointer.new(:uint32, 16)
      @keyevent_code_offset = SFML::KeyEvent.offset_of(:code)
      @sizeevent_width_offset = SFML::SizeEvent.offset_of(:width)
      @sizeevent_height_offset = SFML::SizeEvent.offset_of(:height)
      @key_mapping = DEFAULT_KEY_MAPPING
    end

    def dispose
    end

    DEFAULT_KEY_MAPPING = {
      57 => [:start, 0],  # space
      58 => [:select, 0], # return
      25 => [:a, 0],      # `Z'
      23 => [:b, 0],      # `X'
      72 => [:right, 0],  # right
      71 => [:left, 0],   # left
      74 => [:down, 0],   # down
      73 => [:up, 0],     # up

      # 57 => [:start, 1],  # space
      # 58 => [:select, 1], # return
      # 25 => [:a, 1],      # `Z'
      # 23 => [:b, 1],      # `X'
      # 72 => [:right, 1],  # right
      # 71 => [:left, 1],   # left
      # 74 => [:down, 1],   # down
      # 73 => [:up, 1],     # up

      27 => [:screen_x1, nil],   # `1'
      28 => [:screen_x2, nil],   # `2'
      29 => [:screen_x3, nil],   # `3'
      5  => [:screen_full, nil], # `f'
      16 => [:quit, nil],        # `q'
    }

    def tick(_frame, pads)
      SFML.sfRenderWindow_setKeyRepeatEnabled(@video.window, 0)

      while SFML.sfRenderWindow_pollEvent(@video.window, @event) != 0
        case @event.read_int
        when 0 # EvtClosed
          SFML.sfRenderWindow_close(@video.window)
          exit # tmp
        when 1 # EvtResized
          w = @event.get_int(@sizeevent_width_offset)
          h = @event.get_int(@sizeevent_height_offset)
          @video.on_resize(w, h)
        when 5 # EvtKeyPressed
          event(pads, :keydown, *@key_mapping[@event.get_int(@keyevent_code_offset)])
        when 6 # EvtKeyReleased
          event(pads, :keyup, *@key_mapping[@event.get_int(@keyevent_code_offset)])
        when 14 # sfEvtJoystickButtonPressed
          # XXX
        when 15 # sfEvtJoystickButtonReleased
          # XXX
        when 16 # sfEvtJoystickMoved
          # XXX
        when 17 # sfEvtJoystickConnected
          # XXX
        when 18 # sfEvtJoystickDisconnected
          # XXX
        end
      end
    end
  end
end
