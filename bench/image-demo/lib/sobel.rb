require_relative 'noborder'

def sobeldx(img)
  img.map { |p|
    (-1.0 * img[p + [-1,-1]] + 1.0 * img[p + [1,-1]] +
     -2.0 * img[p + [-1, 0]] + 2.0 * img[p + [1, 0]] +
     -1.0 * img[p + [-1, 1]] + 1.0 * img[p + [1, 1]]) / 4.0
  }
end

def sobeldy(img)
  img.map { |p|
    (-1.0*img[p + [-1,-1]] -2.0*img[p + [0,-1]] -1.0*img[p + [1,-1]] +
      1.0*img[p + [-1, 1]] +2.0*img[p + [0, 1]] +2.0*img[p + [1, 1]]) / 4.0
  }
end

def sobel_magnitude(img)
  img.map { |p|
    dx = -1.0 * img[p + [-1,-1]] + 1.0 * img[p + [1,-1]] +
         -2.0 * img[p + [-1, 0]] + 2.0 * img[p + [1, 0]] +
         -1.0 * img[p + [-1, 1]] + 1.0 * img[p + [1, 1]]
    dy = -1.0*img[p + [-1,-1]] -2.0*img[p + [0,-1]] -1.0*img[p + [1,-1]] +
          1.0*img[p + [-1, 1]] +2.0*img[p + [0, 1]] +1.0*img[p + [1, 1]]
    Mat.sqrt(dx*dx + dy*dy) / 4.0
  }
end

def uint8(img)
  img.map { |p|
    [[img[p], 0].max, 255].min
  }
end

def sobel_magnitude_uint8(img)
  img.map { |p|
    dx = -1.0 * img[p + [-1,-1]] + 1.0 * img[p + [1,-1]] +
         -2.0 * img[p + [-1, 0]] + 2.0 * img[p + [1, 0]] +
         -1.0 * img[p + [-1, 1]] + 1.0 * img[p + [1, 1]]
    dy = -1.0*img[p + [-1,-1]] -2.0*img[p + [0,-1]] -1.0*img[p + [1,-1]] +
          1.0*img[p + [-1, 1]] +2.0*img[p + [0, 1]] +1.0*img[p + [1, 1]]
    [(Math.sqrt(dx*dx + dy*dy) / 4.0).to_i, 255].min
  }
end

def main
  image_class = Object.const_get(ARGV.first)
  n = 1000
  if ARGV.size == 1
    10.times { |i|
      sobel_magnitude(image_class.new(n, n))
    }
    return 'sobel(%s(%dx%d))' % [image_class, n, n]
  else
    10.times { |i|
      sobel_magnitude_uint8(image_class(n, n, typecode='B'))
    }
    return 'sobel_uint8(%s(%dx%d))' % [image_class, n, n]
  end
end

if $0 == __FILE__
  require_relative 'io'

  if ARGV.size >= 1
    fn = ARGV.first
  else
    fn = 'test.avi -vf scale=640:480 -benchmark'
  end

  start = start0 = Time.now
  mplayer(NoBorderImagePadded, fn).each_with_index do |img, frame|
    #view(img)
    #sobeldx(img)
    #view(uint8(sobel_magnitude(img)))
    #sobel_magnitude_uint8(img)
    begin
      view(sobel_magnitude_uint8(img))
    rescue Errno::EPIPE
      puts 'Exiting'
      break
    end

    t = Time.now
    puts "%.3f fps %.3f average fps" % [1.0 / (t - start), (frame-2) / (t - start0)]
    start = t
    if frame == 2
      start0 = Time.now
    end
  end
end
