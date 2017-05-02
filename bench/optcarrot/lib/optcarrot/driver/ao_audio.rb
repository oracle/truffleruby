require "ffi"

module Optcarrot
  # A minimal binding for libao
  module Ao
    extend FFI::Library
    ffi_lib "ao"

    # struct ao_sample_format
    class SampleFormat < FFI::Struct
      layout(
        :bits, :int,
        :rate, :int,
        :channels, :int,
        :byte_format, :int,
        :matrix, :pointer,
      )
    end

    FMT_NATIVE = 4

    {
      initialize: [[], :void],
      default_driver_id: [[], :int],
      open_live: [[:int, :pointer, :pointer], :pointer],
      play: [[:pointer, :pointer, :int], :uint32, blocking: true],
      close: [[:pointer], :int],
      shutdown: [[], :void],
    }.each do |name, params|
      attach_function(name, :"ao_#{ name }", *params)
    end
  end

  # Audio output driver for libao
  class AoAudio < Audio
    def init
      format = Ao::SampleFormat.new
      format[:bits] = @bits
      format[:rate] = @rate
      format[:channels] = 1
      format[:byte_format] = Ao::FMT_NATIVE
      format[:matrix] = nil

      Ao.initialize
      driver = Ao.default_driver_id
      @dev = Ao.open_live(driver, format, nil)

      @conf.fatal("ao_open_live failed") unless @dev
      @buff = "".b
    end

    def dispose
      Ao.close(@dev)
      Ao.shutdown
    end

    def tick(output)
      buff = output.pack(@pack_format)
      Ao.play(@dev, buff, buff.bytesize)
    end
  end
end
