require_relative "misc"

module Optcarrot
  # Video output driver saving an animation GIF file
  class GIFVideo < Video
    def init
      super

      @f = open(File.basename(@conf.video_output) + ".gif", "wb")

      @palette, colors = Driver.quantize_colors(@palette_rgb)

      # GIF Header
      header = ["GIF89a", WIDTH, HEIGHT, 0xf7, 0, 0, *colors.flatten]
      @f << header.pack("A*vvC*")

      # Application Extension
      @f << [0x21, 0xff, 0x0b, "NETSCAPE", "2.0", 0x03, 0x01, 0x00, 0x00].pack("C3A8A3CCvC")

      # Graphic Control Extension
      @header = [0x21, 0xf9, 0x04, 0x00, 1, 255, 0x00].pack("C4vCC")
      @header << [0x2c, 0, 0, WIDTH, HEIGHT, 0, 8].pack("Cv4C*")
    end

    def dispose
      # Trailer
      @f << [0x3b].pack("C")

      @f.close
    end

    def tick(screen)
      compress(screen)
      super
    end

    def compress(data)
      @f << @header

      max_code = 257
      dict = (0..max_code).map {|n| [n, []] }

      buff = ""
      out = ->(code) { buff << ("%0#{ max_code.bit_length }b" % code).reverse }

      cur_dict = dict
      code = nil
      out[256] # clear code
      data.each do |d|
        if cur_dict[d]
          code, cur_dict = cur_dict[d]
        else
          out[code]
          if max_code < 4094
            max_code += 1
            cur_dict[d] = [max_code, []]
          end
          code, cur_dict = dict[d]
        end
      end
      out[code]
      out[257] # end code

      buff = [buff].pack("b*")

      buff = buff.gsub(/.{1,255}/m) { [$&.size].pack("C") + $& } + [0].pack("C")

      @f << buff
    end
  end
end
