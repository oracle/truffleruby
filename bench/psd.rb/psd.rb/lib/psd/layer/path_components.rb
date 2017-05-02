class PSD
  class Layer
    module PathComponents
      # Attempt to translate this layer and modify the document.
      def translate(x=0, y=0)
        @left += x
        @right += x
        @top += y
        @bottom += y

        @path_components.each{ |p| p.translate(x,y) } if @path_components
      end

      # Attempt to scale the path components within this layer.
      def scale_path_components(xr, yr)
        return unless @path_components

        @path_components.each{ |p| p.scale(xr, yr) }
      end
    end
  end
end