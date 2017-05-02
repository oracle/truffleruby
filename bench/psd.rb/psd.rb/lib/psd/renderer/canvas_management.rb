class PSD
  class Renderer
    module CanvasManagement
      def active_canvas
        @canvas_stack.last
      end

      def create_group_canvas(node, width=@width, height=@height)
        PSD.logger.debug "Group canvas created. Node = #{node.name || ":root:"}, width = #{width}, height = #{height}"
        push_canvas Canvas.new(node, width, height)
      end

      def push_canvas(canvas)
        @canvas_stack << canvas
      end

      def pop_canvas
        @canvas_stack.pop
      end

      def stack_inspect
        @canvas_stack.map { |c| c.node.name || ":root:" }.join("\n")
      end
    end
  end
end