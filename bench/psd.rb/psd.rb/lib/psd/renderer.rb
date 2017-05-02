require_relative 'renderer/canvas_management'

class PSD
  class Renderer
    include CanvasManagement

    def initialize(node)
      @root_node = node

      # Our canvas always starts as the full document size because
      # all measurements are relative to this size. We can later crop
      # the image if needed.
      @width = @root_node.document_dimensions[0].to_i
      @height = @root_node.document_dimensions[1].to_i

      @canvas_stack = []
      @node_stack = [@root_node]

      @rendered = false
    end

    def render!
      PSD.logger.debug "Beginning render process"

      # Create our base canvas
      create_group_canvas(active_node, active_node.width, active_node.height)

      # Begin the rendering process
      execute_pipeline

      @rendered = true
    end

    def execute_pipeline
      PSD.logger.debug "Executing pipeline on #{active_node.debug_name}"
      children.reverse.each do |child|
        # We skip over hidden nodes. Maybe something configurable in the future?
        next unless child.visible?

        if child.group?
          push_node(child)

          if child.passthru_blending?
            PSD.logger.debug "#{child.name} is a group with passthru blending"
            execute_pipeline
          else
            PSD.logger.debug "#{child.name} is a group with #{child.blending_mode} blending"
            
            create_group_canvas(child)
            execute_pipeline
            
            child_canvas = pop_canvas
            child_canvas.paint_to active_canvas
          end

          pop_node and next
        end

        canvas = Canvas.new(child)
        canvas.paint_to active_canvas
      end
    end

    def to_png
      render! unless @rendered
      active_canvas.canvas
    end

    private

    def children
      if active_node.layer?
        [active_node]
      else
        active_node.children
      end
    end

    def push_node(node)
      @node_stack << node
    end

    def pop_node
      @node_stack.pop
    end

    def active_node
      @node_stack.last
    end
  end
end