module Optcarrot
  # some helper methods for drivers
  module Driver
    module_function

    def quantize_colors(colors, limit = 256)
      # median-cut
      @cubes = [colors.uniq]
      (limit - 1).times do
        cube = @cubes.pop
        axis = (0..2).max_by do |a|
          min, max = cube.map {|color| color[a] }.minmax
          max - min
        end
        cube = cube.sort_by {|color| color[axis] }
        @cubes << cube[0, cube.size / 2] << cube[(cube.size / 2)..-1]
        @cubes.sort_by! {|a| a.size }
      end
      raise if @cubes.size != limit
      idx = 0
      mapping = {}
      unified_colors = @cubes.map do |cube|
        cube.each {|color| mapping[color] = idx }
        idx += 1
        cube.transpose.map {|ary| ary.inject(&:+) / ary.size }
      end
      palette = colors.map {|color| mapping[color] }
      return palette, unified_colors
    end

    def cutoff_overscan(colors)
      colors[0, 2048] = EMPTY_ARRAY
      colors[-2048, 2048] = EMPTY_ARRAY
    end
    EMPTY_ARRAY = []

    SIZE = 1
    def show_fps(colors, fps, palette)
      # darken the right-bottom corner for drawing FPS
      (223 - 6 * SIZE).upto(223) do |y|
        (255 - 20 * SIZE).upto(255) do |x|
          c = colors[idx = x + y * 256]
          r = ((c >> 16) & 0xff) / 4
          g = ((c >>  8) & 0xff) / 4
          b = ((c >>  0) & 0xff) / 4
          colors[idx] = (c & 0xff000000) | (r << 16) | (g << 8) | b
        end
      end

      # decide fps color
      color =
        case
        when fps >= 60 then palette[0x11] # blue
        when fps >= 55 then palette[0x28] # yellow
        else                palette[0x16] # red
        end

      # draw FPS
      5.times do |i| # show "xxFPS"
        bits = FONT[i <= 1 ? fps / 10**(1 - i) % 10 : i + 8]
        5.times do |y|
          3.times do |x|
            SIZE.times do |dy|
              SIZE.times do |dx|
                if bits[x + y * 3] == 1
                  colors[(224 + (y - 6) * SIZE + dy) * 256 + (256 + i * 4 + x - 20) * SIZE + dx] = color
                end
              end
            end
          end
        end
      end
    end

    # tiny font data for fps
    FONT = [
      0b111_101_101_101_111, # '0'
      0b111_010_010_011_010, # '1'
      0b111_001_111_100_111, # '2'
      0b111_100_111_100_111, # '3'
      0b100_100_111_101_101, # '4'
      0b111_100_111_001_111, # '5'
      0b111_101_111_001_111, # '6'
      0b010_010_100_101_111, # '7'
      0b111_101_111_101_111, # '8'
      0b111_100_111_101_111, # '9'
      0b001_001_111_001_111, # 'F'
      0b001_011_101_101_011, # 'P'
      0b011_100_010_001_110, # 'S'
    ]

    # icon data
    def icon_data
      width, height = 16, 16
      pixels = FFI::MemoryPointer.new(:uint8, width * height * 4)

      palette = [
        0x00000000, 0xff0026ff, 0xff002cda, 0xff004000, 0xff0050ff, 0xff006000, 0xff007aff, 0xff00a000, 0xff00a4ff,
        0xff00e000, 0xff4f5600, 0xffa0a000, 0xffe0e000]
      dat = "38*2309(3:9&,8210982(32,=&8*1:=2,9=1#5$(2&3'?%(-@715+)A3'?'A-.<0$$++B1:$?B6<0$++)$43#%)'A@<:%B314@.<1"
      i = 66
      "54'4-6>')+((;/7#0#,2,*//..'$%-11*(00##".scan(/../) do
        dat = dat.gsub(i.chr, $&)
        i -= 1
      end
      dat = dat.bytes.map {|clr| palette[clr - 35] }

      return width, height, pixels.write_string(dat.pack("V*"))
    end
  end
end
