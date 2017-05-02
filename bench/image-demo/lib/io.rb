# Read from stream and yield images
def mplayer(image_class, fn = 'tv://', options = '')
  return to_enum(__method__, image_class, fn, options) unless block_given?

  IO.popen("mplayer -really-quiet -noframedrop #{options} -vo yuv4mpeg:file=/dev/stdout 2>/dev/null </dev/null #{fn}") do |f|
    header = f.readline
    raise "Bad header" unless /W(\d+) H(\d+)/ =~ header
    w, h = Integer($1), Integer($2)
    loop {
      header = f.readline
      break if header != "FRAME\n"
      yield image_class.new(w, h, f)
      f.read(w*h/2) # Color data
    }
  end
end

# Render images with MPlayer
class MPlayerViewer
  def view(img)
    unless @width
      @mplayer = IO.popen('mplayer -really-quiet -noframedrop - 2>/dev/null', 'wb')
      @mplayer.puts("YUV4MPEG2 W#{img.width} H#{img.height} F100:1 Ip A1:1")
      @width = img.width
      @height = img.height
      #@color_data = Array.new(img.width * img.height / 2, 127).pack("C*")
      @color_data = 127.chr * (img.width * img.height / 2)
    end
    raise unless @width == img.width and @height == img.height
    @mplayer.write "FRAME\n"
    img.tofile(@mplayer)
    @mplayer.write @color_data
  end
end

DEFAULT_VIEWER = MPlayerViewer.new

def view(img)
  DEFAULT_VIEWER.view(img)
end
