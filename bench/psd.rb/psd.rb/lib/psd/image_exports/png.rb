class PSD::Image
  module Export
    # PNG image export. This is the default export format.
    module PNG
      # Load the image pixels into a PNG file and return a reference to the
      # data.
      def to_png
        return @png if @png

        PSD.logger.debug "Beginning PNG export"
        @png = ChunkyPNG::Canvas.new(width.to_i, height.to_i, ChunkyPNG::Color::TRANSPARENT)

        i = 0
        height.times do |y|
          width.times do |x|
            @png[x, y] = @pixel_data[i]
            i += 1
          end
        end

        @png
      end
      alias :export :to_png

      # Saves the PNG data to disk.
      def save_as_png(file)
        to_png.save(file, :fast_rgba)
      end
    end
  end
end