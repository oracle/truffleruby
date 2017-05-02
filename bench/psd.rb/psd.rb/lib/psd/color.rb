class PSD
  # Various color conversion methods. All color values are stored in the PSD
  # document in the color space as defined by the user instead of a normalized
  # value of some kind. This means that we have to do all the conversion ourselves
  # for each color space.
  module Color
    extend self

      # This is a relic of libpsd that will likely go away in a future version. It
      # stored the entire color value in a 32-bit address space for speed.
    def color_space_to_argb(color_space, color_component)
      color = case color_space
      when 0
        rgb_to_color *color_component
      when 1
        hsb_to_color color_component[0], 
          color_component[1] / 100.0, color_component[2] / 100.0
      when 2
        cmyk_to_color color_component[0] / 100.0,
          color_component[1] / 100.0, color_component[2] / 100.0,
          color_component[3] / 100.0
      when 7
        lab_to_color *color_component
      else
        0x00FFFFFF
      end

      color_to_argb(color)
    end

    def color_to_argb(color)
      [
        (color) >> 24,
        ((color) & 0x00FF0000) >> 16,
        ((color) & 0x0000FF00) >> 8,
        (color) & 0x000000FF
      ]
    end

    def rgb_to_color(*args)
      argb_to_color(255, *args)
    end

    def argb_to_color(a, r, g, b)
      (a << 24) | (r << 16) | (g << 8) | b
    end

    def hsb_to_color(*args)
      ahsb_to_color(255, *args)
    end

    def ahsb_to_color(alpha, hue, saturation, brightness)
      if saturation == 0.0
        b = g = r = (255 * brightness).to_i
      else
        if brightness <= 0.5
          m2 = brightness * (1 + saturation)
        else
          m2 = brightness + saturation - brightness * saturation
        end

        m1 = 2 * brightness - m2
        r = hue_to_color(hue + 120, m1, m2)
        g = hue_to_color(hue, m1, m2)
        b = hue_to_color(hue - 120, m1, m2)
      end

      argb_to_color alpha, r, g, b
    end

    def hue_to_color(hue, m1, m2)
      hue = (hue % 360).to_i
      if hue < 60
        v = m1 + (m2 - m1) * hue / 60
      elsif hue < 180
        v = m2
      elsif hue < 240
        v = m1 + (m2 - m1) * (240 - hue) / 60
      else
        v = m1
      end

      (v * 255).to_i
    end

    def cmyk_to_color(c, m, y, k)
      r = 1 - (c * (1 - k) + k) * 255
      g = 1 - (m * (1 - k) + k) * 255
      b = 1 - (y * (1 - k) + k) * 255

      r = [0, r, 255].sort[1]
      g = [0, g, 255].sort[1]
      b = [0, b, 255].sort[1]

      rgb_to_color r, g, b
    end

    def lab_to_color(*args)
      alab_to_color(255, *args)
    end

    def alab_to_color(alpha, l, a, b)
      xyz = lab_to_xyz(l, a, b)
      axyz_to_color alpha, xyz[:x], xyz[:y], xyz[:z]
    end

    def lab_to_xyz(l, a, b)
      y = (l + 16) / 116
      x = y + (a / 500)
      z = y - (b / 200)

      x, y, z = [x, y, z].map do |n|
        n**3 > 0.008856 ? n**3 : (n - 16 / 116) / 7.787
      end
    end

    def cmyk_to_rgb(c, m, y, k)
      Hash[{
        r: (65535 - (c * (255 - k) + (k << 8))) >> 8,
        g: (65535 - (m * (255 - k) + (k << 8))) >> 8,
        b: (65535 - (y * (255 - k) + (k << 8))) >> 8
      }.map { |k, v| [k, Util.clamp(v, 0, 255)] }]
    end
  end
end