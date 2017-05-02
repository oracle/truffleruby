require_relative 'node'

class PSD::Node
  # Represents the root node of a Photoshop document
  class Root < PSD::Node
    include PSD::HasChildren
    include PSD::Node::ParseLayers

    attr_accessor :children
    attr_reader :psd

    # Stores a reference to the parsed PSD and builds the
    # tree hierarchy.
    def initialize(psd)
      @psd = psd
      build_hierarchy
    end

    # Recursively exports the hierarchy to a Hash
    def to_hash
      {
        children: children.map(&:to_hash),
        document: {
          width: document_width,
          height: document_height,
          resources: {
            layer_comps: @psd.layer_comps,
            guides: @psd.guides,
            slices: @psd.slices
          }
        }
      }
    end

    # Returns the width and height of the entire PSD document.
    def document_dimensions
      [document_width, document_height]
    end

    # The width of the full PSD document as defined in the header.
    def document_width
      @psd.header.width.to_i
    end
    alias_method :width, :document_width

    # The height of the full PSD document as defined in the header.
    def document_height
      @psd.header.height.to_i
    end
    alias_method :height, :document_height

    # The root node has no name since it's not an actual layer or group.
    def name
      nil
    end

    # The depth of the root node is always 0.
    def depth
      0
    end

    [:top, :right, :bottom, :left].each do |meth|
      define_method(meth) { 0 }
    end

    def opacity; 255; end
    def fill_opacity; 255; end

    private

    def build_hierarchy
      @children = []
      result = { layers: [] }
      parseStack = []

      # First we build the hierarchy
      @psd.layers.each do |layer|
        if layer.folder?
          parseStack << result
          result = { name: layer.name, layer: layer, layers: [] }
        elsif layer.folder_end?
          temp = result
          result = parseStack.pop
          result[:layers] << temp
        else
          result[:layers] << layer
        end
      end

      parse_layers(result[:layers])
    end
  end
end