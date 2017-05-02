require "ffi"

module Optcarrot
  # A minimal binding for SDL2
  module SDL2
    extend FFI::Library
    ffi_lib "SDL2"

    # struct SDL_Version
    class Version < FFI::Struct
      layout(
        :major, :uint8,
        :minor, :uint8,
        :patch, :uint8,
      )
    end

    INIT_TIMER    = 0x00000001
    INIT_AUDIO    = 0x00000010
    INIT_VIDEO    = 0x00000020
    INIT_JOYSTICK = 0x00000200

    # Video

    WINDOWPOS_UNDEFINED       = 0x1fff0000
    WINDOW_FULLSCREEN         = 0x00000001
    WINDOW_OPENGL             = 0x00000002
    WINDOW_SHOWN              = 0x00000004
    WINDOW_HIDDEN             = 0x00000008
    WINDOW_BORDERLESS         = 0x00000010
    WINDOW_RESIZABLE          = 0x00000020
    WINDOW_MINIMIZED          = 0x00000040
    WINDOW_MAXIMIZED          = 0x00000080
    WINDOW_INPUT_GRABBED      = 0x00000100
    WINDOW_INPUT_FOCUS        = 0x00000200
    WINDOW_MOUSE_FOCUS        = 0x00000400
    WINDOW_FULLSCREEN_DESKTOP = (WINDOW_FULLSCREEN | 0x00001000)

    pixels = FFI::MemoryPointer.new(:uint32)
    pixels.write_int32(0x04030201)
    PACKEDORDER =
      case pixels.read_bytes(4).unpack("C*")
      when [1, 2, 3, 4] then 3 # PACKEDORDER_ARGB
      when [4, 3, 2, 1] then 8 # PACKEDORDER_BGRA
      else
        raise "unknown endian"
      end

    PIXELFORMAT_8888 =
      (1 << 28) |
      (6 << 24) | # PIXELTYPE_PACKED32
      (PACKEDORDER << 20) |
      (6 << 16) | # PACKEDLAYOUT_8888
      (32 << 8) | # bits
      (4 << 0)    # bytes

    TEXTUREACCESS_STREAMING = 1

    # Input

    # struct SDL_KeyboardEvent
    class KeyboardEvent < FFI::Struct
      layout(
        :type, :uint32,
        :timestamp, :uint32,
        :windowID, :uint32,
        :state, :uint8,
        :repeat, :uint8,
        :padding2, :uint8,
        :padding3, :uint8,
        :scancode, :int,
        :sym, :int,
      )
    end

    # struct SDL_JoyAxisEvent
    class JoyAxisEvent < FFI::Struct
      layout(
        :type, :uint32,
        :timestamp, :uint32,
        :which, :uint32,
        :axis, :uint8,
        :padding1, :uint8,
        :padding2, :uint8,
        :padding3, :uint8,
        :value, :int16,
        :padding4, :uint16,
      )
    end

    # struct SDL_JoyButtonEvent
    class JoyButtonEvent < FFI::Struct
      layout(
        :type, :uint32,
        :timestamp, :uint32,
        :which, :uint32,
        :button, :uint8,
        :state, :uint8,
        :padding1, :uint8,
        :padding2, :uint8,
      )
    end

    # struct SDL_JoyDeviceEvent
    class JoyDeviceEvent < FFI::Struct
      layout(
        :type, :uint32,
        :timestamp, :uint32,
        :which, :int32,
      )
    end

    # Audio

    AUDIO_S8     = 0x8008
    AUDIO_S16LSB = 0x8010
    AUDIO_S16MSB = 0x9010

    pixels = FFI::MemoryPointer.new(:uint16)
    pixels.write_int16(0x0201)
    AUDIO_S16SYS =
      case pixels.read_bytes(2).unpack("C*")
      when [1, 2] then AUDIO_S16LSB
      when [2, 1] then AUDIO_S16MSB
      else
        raise "unknown endian"
      end

    # struct SDL_AudioSpec
    class AudioSpec < FFI::Struct
      layout(
        :freq, :int,
        :format, :uint16,
        :channels, :uint8,
        :silence, :uint8,
        :samples, :uint16,
        :padding, :uint16,
        :size, :uint32,
        :callback, :pointer,
        :userdata, :pointer,
      )
    end

    # rubocop:disable Style/MethodName
    def self.AudioCallback(blk)
      FFI::Function.new(:void, [:pointer, :pointer, :int], blk)
    end
    # rubocop:enable Style/MethodName

    # attach_functions

    functions = {
      InitSubSystem: [[:uint32], :int],
      QuitSubSystem: [[:uint32], :void, blocking: true],
      Delay: [[:int], :void, blocking: true],
      GetError: [[], :string],
      GetTicks: [[], :uint32],

      CreateWindow: [[:string, :int, :int, :int, :int, :uint32], :pointer],
      DestroyWindow: [[:pointer], :void],
      CreateRenderer: [[:pointer, :int, :uint32], :pointer],
      DestroyRenderer: [[:pointer], :void],
      CreateRGBSurfaceFrom: [[:pointer, :int, :int, :int, :int, :uint32, :uint32, :uint32, :uint32], :pointer],
      FreeSurface: [[:pointer], :void],
      GetWindowFlags: [[:pointer], :uint32],
      SetWindowFullscreen: [[:pointer, :uint32], :int],
      SetWindowSize: [[:pointer, :int, :int], :void],
      SetWindowTitle: [[:pointer, :string], :void],
      SetWindowIcon: [[:pointer, :pointer], :void],
      SetHint: [[:string, :string], :int],
      RenderSetLogicalSize: [[:pointer, :int, :int], :int],
      CreateTexture: [[:pointer, :uint32, :int, :int, :int], :pointer],
      DestroyTexture: [[:pointer], :void],
      PollEvent: [[:pointer], :int],
      UpdateTexture: [[:pointer, :pointer, :pointer, :int], :int],
      RenderClear: [[:pointer], :int],
      RenderCopy: [[:pointer, :pointer, :pointer, :pointer], :int],
      RenderPresent: [[:pointer], :int],

      OpenAudioDevice: [[:string, :int, AudioSpec.ptr, AudioSpec.ptr, :int], :uint32, blocking: true],
      PauseAudioDevice: [[:uint32, :int], :void, blocking: true],
      CloseAudioDevice: [[:uint32], :void, blocking: true],

      NumJoysticks: [[], :int],
      JoystickOpen: [[:int], :pointer],
      JoystickClose: [[:pointer], :void],
      JoystickNameForIndex: [[:int], :string],
      JoystickNumAxes: [[:pointer], :int],
      JoystickNumButtons: [[:pointer], :int],
      JoystickInstanceID: [[:pointer], :uint32],

      QueueAudio: [[:uint32, :pointer, :int], :int],
      GetQueuedAudioSize: [[:uint32], :uint32],
      ClearQueuedAudio: [[:uint32], :void],
    }

    # check SDL version

    attach_function(:GetVersion, :SDL_GetVersion, [:pointer], :void)
    version = Version.new
    GetVersion(version)
    version = [version[:major], version[:minor], version[:patch]]
    if (version <=> [2, 0, 4]) < 0
      functions.delete(:QueueAudio)
      functions.delete(:GetQueuedAudioSize)
      functions.delete(:ClearQueuedAudio)
    end

    functions.each do |name, params|
      attach_function(name, :"SDL_#{ name }", *params)
    end
  end
end
