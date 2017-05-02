class PSD
  class Renderer
    class Blender
      attr_reader :fg, :bg

      # Takes a foreground Canvas and a background Canvas
      def initialize(fg, bg)
        @fg = fg
        @bg = bg

        @opacity = @fg.opacity.to_i
        @fill_opacity = @fg.fill_opacity.to_i
        PSD.logger.debug "Blender: name = #{fg.node.name}, opacity = #{@opacity}, fill opacity = #{@fill_opacity}"
      end

      # Composes the foreground Canvas onto the background Canvas using the
      # blending mode specified by the foreground.
      def compose!
        PSD.logger.debug "Composing #{fg.node.debug_name} onto #{bg.node.debug_name} with #{fg.node.blending_mode} blending"

        offset_x = fg.left - bg.left
        offset_y = fg.top - bg.top

        fg.height.times do |y|
          fg.width.times do |x|
            base_x = x + offset_x
            base_y = y + offset_y

            next if base_x < 0 || base_y < 0 || base_x >= bg.width || base_y >= bg.height

            color = Compose.send(
              fg.node.blending_mode,
              fg.canvas[x, y],
              bg.canvas[base_x, base_y],
              compose_options
            )

            bg.canvas[base_x, base_y] = color
          end
        end
      end

      private

      def compose_options
        {
          opacity: @opacity,
          fill_opacity: @fill_opacity
        }
      end
    end
  end
end