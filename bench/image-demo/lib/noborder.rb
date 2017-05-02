# An image class for people who dont care about border effects
class NoBorderImage
  attr_reader :width, :height, :data

  def initialize(w, h, from_file = nil)
    @width = w
    @height = h
    if from_file
      @data = fromfile.read(w*h).bytes
    else
      @data = Array.new(w*h, 0)
    end
  end

  def initialize_copy(from)
    @data = from.data.dup
  end

  def index(p, y = nil)
    if p.is_a?(Pixel)
      p.idx
    else
      y * @width + p
    end
  end
  private :index

  def [](*p)
    @data[index(*p)]
  end

  def []=(*p, val)
    idx = index(*p)
    raise "invalid index: #{idx}" if idx < 0 or idx >= @data.size
    @data[index(*p)] = val
  end

  def pixel_range
    (0...@width * @height)
  end

  def each_pixel
    pixel_range.each { |i|
      yield Pixel.new(i, self)
    }
  end

  def map
    img = self.class.new(@width, @height)
    each_pixel { |p|
      img[p] = yield(p)
    }
    img
  end

  def setup(data)
    @height.times { |y|
      @width.times { |x|
        self[x, y] = data[y][x]
      }
    }
    self
  end

  def tofile(f)
    f.write @data.pack('C*')
  end
end

class NoBorderImagePadded < NoBorderImage
  def initialize(w, h, fromfile=nil)
    @width = w
    @height = h
    if !fromfile
      @data = Array.new(w*(h+2)+2, 0)
    else
      @data = Array.new(w+1, 0)
      @data += fromfile.read(w*h).bytes
      @data += Array.new(w+1, 0)
    end
  end

  def index(p, y = nil)
    if p.is_a? Pixel
      p.idx
    else
      (y+1) * @width + p + 1
    end
  end
  private :index

  def pixel_range
    (@width + 1 ... (@width+1) * @height + 1)
  end

  def tofile(f)
    f.write @data[(@width+1)...(-@width-1)].pack('C*')
  end
end

class Pixel
  attr_reader :idx, :image

  def initialize(idx, image)
    @idx = idx
    @image = image
  end

  def + other
    x, y = other
    Pixel.new(@idx + y*@image.width + x, @image)
  end
end

def conv3x3(img, k)
  img.map { |p|
    k[2,2]*img[p + [-1,-1]] + k[1,2]*img[p + [0,-1]] + k[0,2]*img[p + [1,-1]] +
    k[2,1]*img[p + [-1, 0]] + k[1,1]*img[p + [0, 0]] + k[0,1]*img[p + [1, 0]] +
    k[2,0]*img[p + [-1, 1]] + k[1,0]*img[p + [0, 1]] + k[0,0]*img[p + [1, 1]]
  }
end

def main
  image_class = Object.const_get(ARGV.first)
  n = 1000
  10.times do
    conv3x3(image_class.new(n, n), image_class.new(3, 3))
  end
  'conv3x3(%s(%dx%d))' % [image_class, n, n]
end

if $0 == __FILE__
  require 'benchmark'
  image_class = Object.const_get(ARGV.first)
  n = 1000

  a = Time.now
  100.times {
    p Benchmark.realtime {
      10.times {
        conv3x3(image_class.new(n, n), image_class.new(3,3))
      }
    }
  }
  b = Time.now
  puts 'conv3x3(%s(%dx%d)):' % [image_class, n, n], b - a
end
