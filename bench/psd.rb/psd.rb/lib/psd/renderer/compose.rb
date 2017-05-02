class PSD
  # Collection of methods that composes two RGBA pixels together
  # in various ways based on Photoshop blend modes.
  #
  # Mostly based on similar code from libpsd.
  module Compose
    extend self

    DEFAULT_OPTS = {
      opacity: 255,
      fill_opacity: 255
    }

    #
    # Normal blend modes
    #

    def normal(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))
      new_r = blend_channel(r(bg), r(fg), mix_alpha)
      new_g = blend_channel(g(bg), g(fg), mix_alpha)
      new_b = blend_channel(b(bg), b(fg), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end
    alias_method :passthru, :normal

    #
    # Subtractive blend modes
    #

    def darken(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))
      new_r = r(fg) <= r(bg) ? blend_channel(r(bg), r(fg), mix_alpha) : r(bg)
      new_g = g(fg) <= g(bg) ? blend_channel(g(bg), g(fg), mix_alpha) : g(bg)
      new_b = b(fg) <= b(bg) ? blend_channel(b(bg), b(fg), mix_alpha) : b(bg)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def multiply(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))
      new_r = blend_channel(r(bg), r(fg) * r(bg) >> 8, mix_alpha)
      new_g = blend_channel(g(bg), g(fg) * g(bg) >> 8, mix_alpha)
      new_b = blend_channel(b(bg), b(fg) * b(bg) >> 8, mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def color_burn(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        if f > 0
          f = ((255 - b) << 8) / f
          f > 255 ? 0 : (255 - f)
        else
          b
        end
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def linear_burn(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      new_r = blend_channel(r(bg), (r(fg) < (255 - r(bg))) ? 0 : r(fg) - (255 - r(bg)), mix_alpha)
      new_g = blend_channel(g(bg), (g(fg) < (255 - g(bg))) ? 0 : g(fg) - (255 - g(bg)), mix_alpha)
      new_b = blend_channel(b(bg), (b(fg) < (255 - b(bg))) ? 0 : b(fg) - (255 - b(bg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    #
    # Additive blend modes
    #

    def lighten(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      new_r = r(fg) >= r(bg) ? blend_channel(r(bg), r(fg), mix_alpha) : r(bg)
      new_g = g(fg) >= g(bg) ? blend_channel(g(bg), g(fg), mix_alpha) : g(bg)
      new_b = b(fg) >= b(bg) ? blend_channel(b(bg), b(fg), mix_alpha) : b(bg)
      
      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def screen(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      new_r = blend_channel(r(bg), 255 - ((255 - r(bg)) * (255 - r(fg)) >> 8), mix_alpha)
      new_g = blend_channel(g(bg), 255 - ((255 - g(bg)) * (255 - g(fg)) >> 8), mix_alpha)
      new_b = blend_channel(b(bg), 255 - ((255 - b(bg)) * (255 - b(fg)) >> 8), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def color_dodge(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        f < 255 ? [(b << 8) / (255 - f), 255].min : b
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def linear_dodge(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      new_r = blend_channel(r(bg), (r(bg) + r(fg)) > 255 ? 255 : r(bg) + r(fg), mix_alpha)
      new_g = blend_channel(g(bg), (g(bg) + g(fg)) > 255 ? 255 : g(bg) + g(fg), mix_alpha)
      new_b = blend_channel(b(bg), (b(bg) + b(fg)) > 255 ? 255 : b(bg) + b(fg), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end


    #
    # Contrasting blend modes
    #

    def overlay(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        if b < 128
          b * f >> 7
        else
          255 - ((255 - b) * (255 - f) >> 7)
        end
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def soft_light(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        c1 = b * f >> 8
        c2 = 255 - ((255 - b) * (255 - f) >> 8)
        ((255 - b) * c1 >> 8) + (b * c2 >> 8)
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def hard_light(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        if f < 128
          b * f >> 7
        else
          255 - ((255 - f) * (255 - b) >> 7)
        end
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def vivid_light(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        if f < 255
          [(b * b / (255 - f) + f * f / (255 - b)) >> 1, 255].min
        else
          b
        end
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def linear_light(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        if b < 255
          [f * f / (255 - b), 255].min
        else
          255
        end
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def pin_light(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      calculate_foreground = Proc.new do |b, f|
        if f >= 128
          [b, (f - 128) * 2].max
        else
          [b, f * 2].min
        end
      end

      new_r = blend_channel(r(bg), calculate_foreground.call(r(bg), r(fg)), mix_alpha)
      new_g = blend_channel(g(bg), calculate_foreground.call(g(bg), g(fg)), mix_alpha)
      new_b = blend_channel(b(bg), calculate_foreground.call(b(bg), b(fg)), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def hard_mix(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      new_r = blend_channel(r(bg), (r(bg) + r(fg) <= 255) ? 0 : 255, mix_alpha)
      new_g = blend_channel(g(bg), (g(bg) + g(fg) <= 255) ? 0 : 255, mix_alpha)
      new_b = blend_channel(b(bg), (b(bg) + b(fg) <= 255) ? 0 : 255, mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    #
    # Inversion blend modes
    #

    def difference(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      new_r = blend_channel(r(bg), (r(bg) - r(fg)).abs, mix_alpha)
      new_g = blend_channel(g(bg), (g(bg) - g(fg)).abs, mix_alpha)
      new_b = blend_channel(b(bg), (b(bg) - b(fg)).abs, mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    def exclusion(fg, bg, opts={})
      return apply_opacity(fg, opts) if fully_transparent?(bg)
      return bg if fully_transparent?(fg)

      mix_alpha, dst_alpha = calculate_alphas(fg, bg, DEFAULT_OPTS.merge(opts))

      new_r = blend_channel(r(bg), r(bg) + r(fg) - (r(bg) * r(fg) >> 7), mix_alpha)
      new_g = blend_channel(g(bg), g(bg) + g(fg) - (g(bg) * g(fg) >> 7), mix_alpha)
      new_b = blend_channel(b(bg), b(bg) + b(fg) - (b(bg) * b(fg) >> 7), mix_alpha)

      rgba(new_r, new_g, new_b, dst_alpha)
    end

    # If the blend mode is missing, we fall back to normal composition.
    def method_missing(method, *args, &block)
      return ChunkyPNG::Color.send(method, *args) if ChunkyPNG::Color.respond_to?(method)
      normal(*args)
    end

    private

    def calculate_alphas(fg, bg, opts)
      opacity = calculate_opacity(opts)
      src_alpha = a(fg) * opacity >> 8
      dst_alpha = a(bg)

      mix_alpha = (src_alpha << 8) / (src_alpha + ((256 - src_alpha) * dst_alpha >> 8))
      dst_alpha = dst_alpha + ((256 - dst_alpha) * src_alpha >> 8)

      return mix_alpha, dst_alpha
    end

    def calculate_opacity(opts)
      opts[:opacity] * opts[:fill_opacity] / 255
    end

    def apply_opacity(color, opts)
      (color & 0xffffff00) | ((color & 0x000000ff) * calculate_opacity(opts) / 255)
    end

    def blend_channel(bg, fg, alpha)
      ((bg << 8) + (fg - bg) * alpha) >> 8
    end

    def blend_alpha(bg, fg)
      bg + ((255 - bg) * fg >> 8)
    end
  end
end