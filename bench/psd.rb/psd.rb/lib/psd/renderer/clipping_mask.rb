class PSD
  class Renderer
    class ClippingMask
      attr_reader :canvas, :mask

      def initialize(canvas)
        @canvas = canvas
        @node = @canvas.node
        
        mask_node = @canvas.node.next_sibling
        @mask = Canvas.new(mask_node)
      end

      def apply!
        return unless @node.clipped?

        PSD.logger.debug "Applying clipping mask #{mask.node.name} to #{@node.name}"

        @canvas.height.times do |y|
          @canvas.width.times do |x|
            doc_x = @canvas.left + x
            doc_y = @canvas.top + y

            mask_x = doc_x - @mask.left
            mask_y = doc_y - @mask.top

            if mask_x < 0 || mask_x > mask.width || mask_y < 0 || mask_y > mask.height
              alpha = 0
            else
              pixel = mask.canvas.pixels[mask_y * mask.width + mask_x]
              alpha = pixel.nil? ? 0 : ChunkyPNG::Color.a(pixel)
            end
            
            color = @canvas[x, y]
            @canvas[x, y] = (color & 0xffffff00) | (ChunkyPNG::Color.a(color) * alpha / 255)
          end
        end
      end
    end
  end
end