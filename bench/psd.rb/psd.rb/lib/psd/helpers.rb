class PSD
  # Various helper methods that make accessing PSD data easier since it's
  # split up among various sections.
  module Helpers
    # Width of the entire PSD document, in pixels.
    def width
      header.cols
    end

    # Height of the entire PSD document, in pixels.
    def height
      header.rows
    end

    # All of the layers in this document, including section divider layers.
    def layers
      layer_mask.layers
    end

    # All of the layers, but filters out the section dividers.
    def actual_layers
      layers.delete_if { |l| l.folder? || l.folder_end? }
    end

    # All of the folders in the document.
    def folders
      layers.select { |l| l.folder? }
    end

    # Constructs a tree of the current document for easy traversal and data access.
    def tree
      @root ||= PSD::Node::Root.new(self)
    end

    def resource(id)
      @resources[id].nil? ? nil : @resources[id].data
    end

    def layer_comps
      resource(:layer_comps).to_a
    end

    def guides
      resource(:guides).to_a
    end

    def slices
      resource(:slices).to_a
    end
  end
end