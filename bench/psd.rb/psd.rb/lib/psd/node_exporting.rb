class PSD
  module NodeExporting #:nodoc:
    def export_node(node, path)
      hide_all_nodes
      node.show!
      node.lock_to_origin

      width_difference_factor = @header.cols.to_f / node.width
      height_difference_factor =  @header.rows.to_f / node.height
      @header.cols, @header.rows = node.width, node.height

      node.scale_path_components(width_difference_factor, height_difference_factor)
      export path
    end

    def hide_all_nodes
      tree.descendants.map(&:hide!)
    end
  end
end