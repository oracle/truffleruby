class PSD
  class Node
    module Search
      # Searches the tree structure for a node at the given path. The path is
      # defined by the layer/folder names. Because the PSD format does not
      # require unique layer/folder names, we always return an array of all
      # found nodes.
      def children_at_path(path, opts={})
        path = path.split('/').delete_if { |p| p == "" } unless path.is_a?(Array)

        query = path.shift
        matches = children.select do |c|
          if opts[:case_sensitive]
            c.name == query
          else
            c.name.downcase == query.downcase
          end
        end

        if path.length == 0
          return matches
        else
          return matches.map { |m| m.children_at_path(path, opts) }.flatten
        end
      end
      alias :children_with_path :children_at_path

      # Given a layer comp ID, name, or :last for last document state, create a new
      # tree with layer/group visibility altered based on the layer comp.
      def filter_by_comp(id)
        if id.is_a?(String)
          comp = psd.layer_comps.select { |c| c[:name] == id }.first
          raise "Layer comp not found" if comp.nil?

          id = comp[:id]
        else
          comp = psd.layer_comps.select { |c| c[:id] == id }.first
          raise "Layer comp not found" if comp.nil?
        end

        root = PSD::Node::Root.new(psd)
        filter_for_comp!(comp, root)

        return root
      end

      private

      def filter_for_comp!(comp, node)
        # Force layers to be visible if they are enabled for the comp
        node.children.each do |c|
          set_visibility(comp, c) if Resource::Section::LayerComps.visibility_captured?(comp)
          set_position(comp, c) if Resource::Section::LayerComps.position_captured?(comp)

          filter_for_comp!(comp, c) if c.group?
        end
      end

      def set_visibility(comp, c)
        visible = true

        c
          .metadata
          .data[:layer_comp]['layerSettings'].each do |l|
            visible = l['enab'] if l.has_key?('enab')
            break if l['compList'].include?(comp[:id])
          end

        c.force_visible = visible
      end

      def set_position(comp, c)
        x = 0
        y = 0

        c
          .metadata
          .data[:layer_comp]['layerSettings'].each do |l|
            next unless l.has_key?('Ofst')
            
            x = l['Ofst']['Hrzn']
            y = l['Ofst']['Vrtc']
            break if l['compList'].include?(comp[:id])
          end

        c.left += x
        c.top += y
      end
    end
  end
end