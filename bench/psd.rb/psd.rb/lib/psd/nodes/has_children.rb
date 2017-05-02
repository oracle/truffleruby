class PSD
  module HasChildren
    # Returns all group/folder children of this node.
    def groups
      @children.select{ |c| c.is_a?(PSD::Group) }
    end

    # Returns all layer children of this node.
    def layers
      @children.select{ |c| c.is_a?(PSD::Layer) }
    end
  end
end