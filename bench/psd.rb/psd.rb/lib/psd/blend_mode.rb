class PSD
  # Describes the blend mode for a single layer or folder.
  class BlendMode < BinData::Record
    endian  :big

    string  :sig, read_length: 4
    string  :blend_key, read_length: 4, trim_value: true
    uint8   :opacity
    uint8   :clipping
    bit8    :flags
    skip    length: 1

    # All of the blend modes are stored in the PSD file with a specific key.
    # This is the mapping of that key to its readable name.
    BLEND_MODES = {
      norm: 'normal',
      dark: 'darken',
      lite: 'lighten',
      hue:  'hue',
      sat:  'saturation',
      colr: 'color',
      lum:  'luminosity',
      mul:  'multiply',
      scrn: 'screen',
      diss: 'dissolve',
      over: 'overlay',
      hLit: 'hard light',
      sLit: 'soft light',
      diff: 'difference',
      smud: 'exclusion',
      div:  'color dodge',
      idiv: 'color burn',
      lbrn: 'linear burn',
      lddg: 'linear dodge',
      vLit: 'vivid light',
      lLit: 'linear light',
      pLit: 'pin light',
      hMix: 'hard mix',
      pass: 'passthru',
      dkCl: 'darker color',
      lgCl: 'lighter color',
      fsub: 'subtract',
      fdiv: 'divide'
    }

    # Get the readable name for this blend mode.
    def mode
      BLEND_MODES[blend_key.strip.to_sym]
    end

    # Set the blend mode with the readable name.
    def mode=(val)
      blend_key = BLEND_MODES.invert[val.strip.downcase]
    end

    # Opacity is stored as an integer between 0-255. This converts the opacity
    # to a percentage value to match the Photoshop interface.
    def opacity_percentage
      opacity * 100 / 255
    end

    def transparency_protected
      flags & 0x01
    end

    # Is this layer/folder visible?
    def visible
      !((flags & (0x01 << 1)) > 0)
    end

    def obsolete
      (flags & (0x01 << 2)) > 0
    end

    def pixel_data_irrelevant
      return nil unless (flags & (0x01 << 3)) > 0
      (flags & (0x01 << 4)) > 0
    end
  end
end