class PSD
  class LayerStyles
    class ColorOverlay
      def self.should_apply?(data)
        data.has_key?('SoFi')
      end

      def initialize(styles)
        @canvas = styles.canvas
        @node = styles.node
        @data = styles.data
      end

      def apply!
        # TODO - implement CMYK color overlay
        return if @node.header.cmyk?

        width = @canvas.width
        height = @canvas.height

        # puts width, height
        # puts @canvas.canvas.width, @canvas.canvas.height

        PSD.logger.debug "Layer style: layer = #{@node.name}, type = color overlay, blend mode = #{blending_mode}"

        height.times do |y|
          width.times do |x|
            pixel = @canvas[x, y]
            alpha = ChunkyPNG::Color.a(pixel)
            next if alpha == 0

            overlay_color = ChunkyPNG::Color.rgba(r, g, b, alpha)
            @canvas[x, y] = Compose.send(blending_mode, overlay_color, pixel)
          end
        end
      end

      private

      def blending_mode
        @blending_mode ||= BlendMode::BLEND_MODES[BLEND_TRANSLATION[overlay_data['Md  ']].to_sym]
      end

      def overlay_data
        @data['SoFi']
      end

      def color_data
        overlay_data['Clr ']
      end

      def r
        @r ||= color_data['Rd  '].round
      end

      def g
        @g ||= color_data['Grn '].round
      end

      def b
        @b ||= color_data['Bl  '].round
      end
    end
  end
end