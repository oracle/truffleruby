require "ffi"

module Optcarrot
  # A minimal binding for SFML (CSFML)
  module SFML
    extend FFI::Library
    ffi_lib \
      ["csfml-system", "csfml-system-2"],
      ["csfml-window", "csfml-window-2"],
      ["csfml-graphics", "csfml-graphics-2"],
      ["csfml-audio", "csfml-audio-2"]

    # struct sfVector2u
    class Vector2u < FFI::Struct
      layout(
        :x, :uint,
        :y, :uint,
      )
    end

    # struct sfVector2f
    class Vector2f < FFI::Struct
      layout(
        :x, :float,
        :y, :float,
      )
    end

    # struct sfVideoMode
    class VideoMode < FFI::Struct
      layout(
        :width, :int,
        :height, :int,
        :bits_per_pixel, :int,
      )
    end

    # struct sfEvent
    class Event < FFI::Struct
      layout(
        :type, :int,
      )
    end

    # struct sfSizeEvent
    class SizeEvent < FFI::Struct
      layout(
        :type, :int,
        :width, :uint,
        :height, :uint,
      )
    end

    # struct sfKeyEvent
    class KeyEvent < FFI::Struct
      layout(
        :type, :int,
        :code, :int,
        :alt, :int,
        :control, :int,
        :shift, :int,
        :sym, :int,
      )
    end

    # struct sfColor
    class Color < FFI::Struct
      layout(
        :r, :uint8,
        :g, :uint8,
        :b, :uint8,
        :a, :uint8,
      )
    end

    # struct sfFloatRect
    class FloatRect < FFI::Struct
      layout(
        :left, :float,
        :top, :float,
        :width, :float,
        :height, :float,
      )
    end

    # struct sfSoundStreamChunk
    class SoundStreamChunk < FFI::Struct
      layout(
        :samples, :pointer,
        :sample_count, :uint,
      )
    end

    # rubocop:disable Style/MethodName
    # typedef sfSoundStreamGetDataCallback
    def self.SoundStreamGetDataCallback(blk)
      FFI::Function.new(:int, [SoundStreamChunk.by_ref, :pointer], blk, blocking: true)
    end
    # rubocop:enable Style/MethodName

    attach_function(:sfClock_create, [], :pointer)
    attach_function(:sfClock_destroy, [:pointer], :void)
    attach_function(:sfClock_getElapsedTime, [:pointer], :int64)
    attach_function(:sfClock_restart, [:pointer], :int64)
    attach_function(:sfRenderWindow_create, [VideoMode.by_value, :pointer, :uint32, :pointer], :pointer)
    attach_function(:sfRenderWindow_clear, [:pointer, Color.by_value], :void)
    attach_function(:sfRenderWindow_drawSprite, [:pointer, :pointer, :pointer], :void, blocking: true)
    attach_function(:sfRenderWindow_display, [:pointer], :void, blocking: true)
    attach_function(:sfRenderWindow_close, [:pointer], :void)
    attach_function(:sfRenderWindow_isOpen, [:pointer], :int)
    attach_function(:sfRenderWindow_pollEvent, [:pointer, :pointer], :int)
    attach_function(:sfRenderWindow_destroy, [:pointer], :void)
    attach_function(:sfRenderWindow_setTitle, [:pointer, :pointer], :void)
    attach_function(:sfRenderWindow_setSize, [:pointer, Vector2u.by_value], :void)
    attach_function(:sfRenderWindow_setFramerateLimit, [:pointer, :int], :void)
    attach_function(:sfRenderWindow_setKeyRepeatEnabled, [:pointer, :int], :void)
    attach_function(:sfRenderWindow_setView, [:pointer, :pointer], :void)
    attach_function(:sfRenderWindow_setIcon, [:pointer, :int, :int, :pointer], :void)
    attach_function(:sfTexture_create, [:int, :int], :pointer)
    attach_function(:sfTexture_updateFromPixels, [:pointer, :pointer, :int, :int, :int, :int], :void, blocking: true)
    attach_function(:sfSprite_create, [], :pointer)
    attach_function(:sfSprite_setTexture, [:pointer, :pointer, :int], :void)
    attach_function(:sfView_create, [], :pointer)
    attach_function(:sfView_createFromRect, [:pointer], :pointer)
    attach_function(:sfView_destroy, [:pointer], :void)
    attach_function(:sfView_reset, [:pointer, FloatRect.by_value], :void)
    attach_function(:sfView_setCenter, [:pointer, Vector2f.by_value], :void)
    attach_function(:sfView_setSize, [:pointer, Vector2f.by_value], :void)
    attach_function(:sfSoundStream_create, [:pointer, :pointer, :uint, :uint, :pointer], :pointer)
    attach_function(:sfSoundStream_destroy, [:pointer], :void, blocking: true)
    attach_function(:sfSoundStream_play, [:pointer], :void)
    attach_function(:sfSoundStream_stop, [:pointer], :void, blocking: true)
  end
end
