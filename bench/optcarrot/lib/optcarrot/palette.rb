module Optcarrot
  # NES palette generators
  module Palette
    module_function

    # I don't know where this palette definition came from, but many emulators uses this
    def defacto_palette
      [
        [1.00, 1.00, 1.00], # default
        [1.00, 0.80, 0.81], # emphasize R
        [0.78, 0.94, 0.66], # emphasize G
        [0.79, 0.77, 0.63], # emphasize RG
        [0.82, 0.83, 1.12], # emphasize B
        [0.81, 0.71, 0.87], # emphasize RB
        [0.68, 0.79, 0.79], # emphasize GB
        [0.70, 0.70, 0.70], # emphasize RGB
      ].flat_map do |rf, gf, bf|
        # RGB default palette (I don't know where this palette came from)
        [
          0x666666, 0x002a88, 0x1412a7, 0x3b00a4, 0x5c007e, 0x6e0040, 0x6c0600, 0x561d00,
          0x333500, 0x0b4800, 0x005200, 0x004f08, 0x00404d, 0x000000, 0x000000, 0x000000,
          0xadadad, 0x155fd9, 0x4240ff, 0x7527fe, 0xa01acc, 0xb71e7b, 0xb53120, 0x994e00,
          0x6b6d00, 0x388700, 0x0c9300, 0x008f32, 0x007c8d, 0x000000, 0x000000, 0x000000,
          0xfffeff, 0x64b0ff, 0x9290ff, 0xc676ff, 0xf36aff, 0xfe6ecc, 0xfe8170, 0xea9e22,
          0xbcbe00, 0x88d800, 0x5ce430, 0x45e082, 0x48cdde, 0x4f4f4f, 0x000000, 0x000000,
          0xfffeff, 0xc0dfff, 0xd3d2ff, 0xe8c8ff, 0xfbc2ff, 0xfec4ea, 0xfeccc5, 0xf7d8a5,
          0xe4e594, 0xcfef96, 0xbdf4ab, 0xb3f3cc, 0xb5ebf2, 0xb8b8b8, 0x000000, 0x000000,
        ].map do |rgb|
          r = [((rgb >> 16 & 0xff) * rf).floor, 0xff].min
          g = [((rgb >>  8 & 0xff) * gf).floor, 0xff].min
          b = [((rgb >>  0 & 0xff) * bf).floor, 0xff].min
          [r, g, b]
        end
      end
    end

    # Nestopia generates a palette systematically (cool!), but it is not compatible with nes-tests-rom
    def nestopia_palette
      (0..511).map do |n|
        tint, level, color = n >> 6 & 7, n >> 4 & 3, n & 0x0f
        level0, level1 = [[-0.12, 0.40], [0.00, 0.68], [0.31, 1.00], [0.72, 1.00]][level]
        level0 = level1 if color == 0x00
        level1 = level0 if color == 0x0d
        level0 = level1 = 0 if color >= 0x0e
        y = (level1 + level0) * 0.5
        s = (level1 - level0) * 0.5
        iq = Complex.polar(s, Math::PI / 6 * (color - 3))
        if tint != 0 && color <= 0x0d
          if tint == 7
            y = (y * 0.79399 - 0.0782838) * 1.13
          else
            level1 = (level1 * (1 - 0.79399) + 0.0782838) * 0.5
            y -= level1 * 0.5
            y -= level1 *= 0.6 if [3, 5, 6].include?(tint)
            iq += Complex.polar(level1, Math::PI / 12 * ([0, 6, 10, 8, 2, 4, 0, 0][tint] * 2 - 7))
          end
        end
        [[105, 0.570], [251, 0.351], [15, 1.015]].map do |angle, gain|
          clr = y + (Complex.polar(gain * 2, (angle - 33) * Math::PI / 180) * iq.conjugate).real
          [0, (clr * 255).round, 255].sort[1]
        end
      end
    end
  end
end
