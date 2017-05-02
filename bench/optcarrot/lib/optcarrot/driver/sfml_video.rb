require_relative "sfml"
require_relative "misc"

module Optcarrot
  # Video output driver for SFML
  class SFMLVideo < Video
    def init
      vm = SFML::VideoMode.new
      vm[:width] = TV_WIDTH
      vm[:height] = HEIGHT
      vm[:bits_per_pixel] = 32
      @window = SFML.sfRenderWindow_create(vm, "optcarrot", 7, nil)
      @texture = SFML.sfTexture_create(WIDTH, HEIGHT)
      @sprite = SFML.sfSprite_create
      SFML.sfRenderWindow_setFramerateLimit(@window, 60)
      SFML.sfSprite_setTexture(@sprite, @texture, 1)
      @color = SFML::Color.new
      @color[:r] = @color[:g] = @color[:b] = 0
      @color[:a] = 255
      @buf = FFI::MemoryPointer.new(:uint8, WIDTH * HEIGHT * 4)

      width, height, pixels = Driver.icon_data
      SFML.sfRenderWindow_setIcon(@window, width, height, pixels)

      @frame = 0
      @fps = 0
      @clock = SFML.sfClock_create
      @vec2u = SFML::Vector2u.new
      @vec2f = SFML::Vector2f.new
      @view = SFML.sfView_create

      on_resize(TV_WIDTH, HEIGHT)

      @palette = @palette_rgb.map do |r, g, b|
        0xff000000 | (b << 16) | (g << 8) | r
      end
    end

    def change_window_size(scale)
      if scale
        @vec2u[:x] = TV_WIDTH * scale
        @vec2u[:y] = HEIGHT * scale
        SFML.sfRenderWindow_setSize(@window, @vec2u)
      end
    end

    def on_resize(w, h)
      @vec2f[:x] = WIDTH / 2
      @vec2f[:y] = HEIGHT / 2
      SFML.sfView_setCenter(@view, @vec2f)

      ratio = w.to_f * WIDTH / TV_WIDTH / h
      if WIDTH < ratio * HEIGHT
        @vec2f[:x] = HEIGHT * ratio
        @vec2f[:y] = HEIGHT
      else
        @vec2f[:x] = WIDTH
        @vec2f[:y] = WIDTH / ratio
      end
      SFML.sfView_setSize(@view, @vec2f)

      SFML.sfRenderWindow_setView(@window, @view)
    end

    attr_reader :window

    def tick(colors)
      if SFML.sfClock_getElapsedTime(@clock) >= 1_000_000
        @fps = @frame
        @frame = 0
        SFML.sfClock_restart(@clock)
      end
      @frame += 1

      Driver.cutoff_overscan(colors)
      Driver.show_fps(colors, @fps, @palette) if @conf.show_fps
      @buf.write_array_of_uint32(colors)
      SFML.sfTexture_updateFromPixels(@texture, @buf, WIDTH, HEIGHT, 0, 0)
      SFML.sfRenderWindow_drawSprite(@window, @sprite, nil)
      SFML.sfRenderWindow_display(@window)
      @fps
    end
  end
end
