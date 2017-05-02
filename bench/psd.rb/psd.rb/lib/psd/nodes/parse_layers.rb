class PSD::Node
  module ParseLayers
    # Organizes the flat layer structure into tree nodes.
    def parse_layers(layers)
      @children = []
      layers.each do |layer|
        if layer.is_a?(Hash)
          node = PSD::Node::Group.new(layer)
        elsif layer.is_a?(PSD::Layer)
          node = PSD::Node::Layer.new(layer)
        end

        node.parent = self
        @children << node
      end
    end
  end
end