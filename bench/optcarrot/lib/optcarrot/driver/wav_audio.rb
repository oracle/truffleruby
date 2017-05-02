module Optcarrot
  # Audio output driver saving a WAV file
  class WAVAudio < Audio
    def init
      @buff = []
    end

    def dispose
      buff = @buff.pack(@pack_format)
      wav = [
        "RIFF", 44 + buff.bytesize, "WAVE", "fmt ", 16, 1, 1,
        @rate, @rate * @bits / 8, @bits / 8, @bits, "data", buff.bytesize, buff
      ].pack("A4VA4A4VvvVVvvA4VA*")
      File.binwrite("audio.wav", wav)
    end

    def tick(output)
      @buff.concat output
    end
  end
end
