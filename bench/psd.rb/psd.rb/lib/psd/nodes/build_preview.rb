 class PSD
  class Node
    module BuildPreview
      def renderer
        PSD::Renderer.new(self)
      end

      def to_png
        renderer.to_png
      end

      def save_as_png(output)
        to_png.save(output)
      end
    end
  end
end
