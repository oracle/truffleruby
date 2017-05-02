require_relative "sfml"

module Optcarrot
  # Audio output driver for SFML
  class SFMLAudio < Audio
    def init
      @max_buff_size = @rate * @bits / 8 * BUFFER_IN_FRAME / NES::FPS

      # we need to prevent this callback object from GC
      @callback = SFML.SoundStreamGetDataCallback(method(:callback))

      @stream = SFML.sfSoundStream_create(@callback, nil, 1, @rate, nil)
      SFML.sfSoundStream_play(@stream)
      @buff = "".b
      @cur_buff = FFI::MemoryPointer.new(:char, @max_buff_size + 1)
    end

    def dispose
      SFML.sfSoundStream_stop(@stream)
      SFML.sfSoundStream_destroy(@stream)
    end

    def tick(output)
      @buff << output.pack("v*".freeze)
    end

    # XXX: support 8bit (SFML supports only 16bit, so translation is required)
    def callback(chunk, _userdata)
      buff_size = @buff.size
      if buff_size < @max_buff_size
        @cur_buff.put_string(0, @buff)
      else
        @buff[0, buff_size - @max_buff_size] = "".freeze
        @cur_buff.put_string(0, @buff)
        buff_size = @max_buff_size
      end
      if buff_size == 0
        @cur_buff.clear
        buff_size = @max_buff_size / BUFFER_IN_FRAME
      end
      chunk[:samples] = @cur_buff
      chunk[:sample_count] = buff_size / 2
      return 1
    end
  end
end
