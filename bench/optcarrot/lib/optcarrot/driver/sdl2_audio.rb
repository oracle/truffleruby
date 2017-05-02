require_relative "sdl2"

module Optcarrot
  # Audio output driver for SDL2
  class SDL2Audio < Audio
    FORMAT = { 8 => SDL2::AUDIO_S8, 16 => SDL2::AUDIO_S16LSB }

    def init
      SDL2.InitSubSystem(SDL2::INIT_AUDIO)
      @max_buff_size = @rate * @bits / 8 * BUFFER_IN_FRAME / NES::FPS

      # we need to prevent this callback object from GC
      @callback = SDL2.AudioCallback(method(:callback))

      desired = SDL2::AudioSpec.new
      desired[:freq] = @rate
      desired[:format] = FORMAT[@bits]
      desired[:channels] = 1
      desired[:samples] = @rate / 60 * 2
      desired[:callback] = defined?(SDL2.QueueAudio) ? nil : @callback
      desired[:userdata] = nil
      obtained = SDL2::AudioSpec.new
      @dev = SDL2.OpenAudioDevice(nil, 0, desired, obtained, 0)
      if @dev == 0
        @conf.error("SDL2_OpenAudioDevice failed: #{ SDL2.GetError }")
        abort
      end
      @buff = "".b
      SDL2.PauseAudioDevice(@dev, 0)
    end

    def dispose
      SDL2.CloseAudioDevice(@dev)
      SDL2.QuitSubSystem(SDL2::INIT_AUDIO)
    end

    def tick(output)
      buff = output.pack(@pack_format)
      if defined?(SDL2.QueueAudio)
        SDL2.QueueAudio(@dev, buff, buff.bytesize)
        SDL2.ClearQueuedAudio(@dev) if SDL2.GetQueuedAudioSize(@dev) > @max_buff_size
      else
        @buff << buff
      end
    end

    # for SDL 2.0.3 or below in that SDL_QueueAudio is not available
    def callback(_userdata, stream, stream_len)
      buff_size = @buff.size
      if stream_len > buff_size
        # stream.clear # is it okay?
        stream.write_string_length(@buff, buff_size)
        @buff.clear
      else
        stream.write_string_length(@buff, stream_len)
        stream_len = buff_size - @max_buff_size if buff_size - stream_len > @max_buff_size
        @buff[0, stream_len] = "".freeze
      end
    end
  end
end
