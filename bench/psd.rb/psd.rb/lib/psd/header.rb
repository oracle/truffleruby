class PSD
  # Describes the Header for the PSD file, which is the first section of the file.
  class Header < BinData::Record
    endian :big

    string  :sig, read_length: 4
    uint16  :version

    # Reserved bytes
    skip    length: 6

    uint16  :channels
    uint32  :rows
    uint32  :cols
    uint16  :depth
    uint16  :mode

    uint32  :color_data_len
    skip    length: :color_data_len

    # All of the color modes are stored internally as a short from 0-15.
    # This is a mapping of that value to a human-readable name.
    MODES = [
      'Bitmap',
      'GrayScale',
      'IndexedColor',
      'RGBColor',
      'CMYKColor',
      'HSLColor',
      'HSBColor',
      'Multichannel',
      'Duotone',
      'LabColor',
      'Gray16',
      'RGB48',
      'Lab48',
      'CMYK64',
      'DeepMultichannel',
      'Duotone16'
    ]

    # Get the human-readable color mode name.
    def mode_name
      if mode >= 0 && mode <= 15
        MODES[mode]
      else
        "(#{mode})"
      end
    end

    # Width of the entire document in pixels.
    def width
      cols
    end

    # Height of the entire document in pixels.
    def height
      rows
    end

    def rgb?
      mode == 3
    end

    def cmyk?
      mode == 4
    end
  end
end