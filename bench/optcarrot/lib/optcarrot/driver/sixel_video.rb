require_relative "misc"

module Optcarrot
  # Video output driver for Sixel (this is a joke feature)
  class SixelVideo < Video
    def init
      super
      @buff = "".b
      @line = "".b
      @seq_setup = "\e[H\ePq"
      print "\e[2J"

      @palette, colors = Driver.quantize_colors(@palette_rgb)

      colors.each_with_index do |rgb, c|
        @seq_setup << "#" << [c, 2, *rgb.map {|clr| clr * 100 / 255 }].join(";")
      end
      @seq_clr = (0..255).map {|c| "##{ c }" }
      @seq_len = (0..256).map {|i| "!#{ i }" }
      @seq_len[1] = ""
      @seq_end = "\e\\"
    end

    def tick(screen)
      @buff.replace(@seq_setup)
      40.times do |y|
        offset = y * 0x600
        six_lines = screen[offset, 0x600]
        six_lines.uniq.each do |c|
          prev_clr = nil
          len = 1
          256.times do |i|
            clr =
              (six_lines[i]         == c ? 0x01 : 0) +
              (six_lines[i + 0x100] == c ? 0x02 : 0) +
              (six_lines[i + 0x200] == c ? 0x04 : 0) +
              (six_lines[i + 0x300] == c ? 0x08 : 0) +
              (six_lines[i + 0x400] == c ? 0x10 : 0) +
              (six_lines[i + 0x500] == c ? 0x20 : 0) + 63
            if prev_clr == clr
              len += 1
            elsif prev_clr
              case len
              when 1 then @line << prev_clr
              when 2 then @line << prev_clr << prev_clr
              else        @line << @seq_len[len] << prev_clr
              end
              len = 1
            end
            prev_clr = clr
          end
          if prev_clr != 63 || len != 256
            @buff << @seq_clr[c] << @line << @seq_len[len] << prev_clr << 36 # $
            @line.clear
          end
        end
        @buff << 45 # -
      end
      print @buff << @seq_end
      super
    end
  end
end
