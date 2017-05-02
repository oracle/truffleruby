class PSD
  class Mask
    attr_reader :size, :top, :left, :bottom, :right, :default_color

    def self.read(file)
      mask = Mask.new(file)
      mask.parse

      mask
    end

    def initialize(file)
      @file = file
      @top = 0
      @left = 0
      @bottom = 0
      @right = 0
    end

    def parse
      @size = @file.read_int
      return if @size == 0

      @mask_end = @file.tell + @size

      @top = @file.read_int
      @left = @file.read_int
      @bottom = @file.read_int
      @right = @file.read_int

      @default_color = @file.read_byte
      @flags = @file.read_byte

      @file.seek @mask_end # Useless info/padding
    end

    def width
      right - left
    end

    def height
      bottom - top
    end

    def relative
      (@flags & 0x01) > 0
    end

    def disabled
      (@flags & (0x01 << 1)) > 0
    end

    def invert
      (@flags & (0x01 << 2)) > 0
    end

    def to_hash
      return {} if @size == 0

      {
        top: top,
        left: left,
        bottom: bottom,
        right: right,
        width: width,
        height: height,
        default_color: default_color,
        relative: relative,
        disabled: disabled,
        invert: invert
      }
    end
  end
end