class PSD
  class Renderer
    class Canvas
      attr_reader :canvas, :node, :width, :height, :left, :right, :top, :bottom, :opacity, :fill_opacity

      def initialize(node, width = nil, height = nil, color = ChunkyPNG::Color::TRANSPARENT)
        @node = node
        @pixel_data = @node.group? ? [] : @node.image.pixel_data
        
        @width = (width || @node.width).to_i
        @height = (height || @node.height).to_i
        @left = @node.left.to_i
        @right = @node.right.to_i
        @top = @node.top.to_i
        @bottom = @node.bottom.to_i

        @opacity = @node.opacity.to_f
        @fill_opacity = @node.fill_opacity.to_f

        @canvas = ChunkyPNG::Canvas.new(@width, @height, color)

        initialize_canvas unless @node.group?
      end

      def paint_to(base)
        PSD.logger.debug "Painting #{node.name} to #{base.node.debug_name}"

        apply_mask
        apply_clipping_mask
        apply_layer_styles
        apply_layer_opacity
        compose_pixels(base)
      end

      def canvas=(canvas)
        @canvas = canvas
        @width = @canvas.width
        @height = @canvas.height
      end

      def [](x, y); @canvas[x, y]; end
      def []=(x, y, value); @canvas[x, y] = value; end

      def method_missing(method, *args, &block)
        @canvas.send(method, *args, &block)
      end

      private

      def initialize_canvas
        PSD.logger.debug "Initializing canvas for #{node.debug_name}"

        # Sorry, ChunkyPNG.
        @canvas.send(:replace_canvas!, width, height, @pixel_data)

        # This can now be referenced by @canvas.pixels
        @pixel_data = nil
      end

      def apply_mask
        return unless @node.image.has_mask?

        PSD.logger.debug "Applying layer mask to #{node.name}"
        Mask.new(self).apply!
      end

      def apply_clipping_mask
        return unless @node.clipped?
        ClippingMask.new(self).apply!
      end

      def apply_layer_styles
        PSD.logger.debug "Applying layer styles to #{node.name}"
        LayerStyles.new(self).apply!
      end

      def apply_layer_opacity
        return if @node.root?
        PSD.logger.debug "Adjusting opacity for #{node.name}"
        
        @node.ancestors.each do |parent|
          break unless parent.passthru_blending?
          @opacity = (@opacity * parent.opacity.to_f) / 255.0
        end

        PSD.logger.debug "Inherited opacity for #{@node.debug_name} is #{@opacity}"
        @opacity = @opacity.to_i
      end

      def compose_pixels(base)
        Blender.new(self, base).compose!
      end
    end
  end
end