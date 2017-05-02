class PSD
  class Node
    # Collection of methods to help in traversing the PSD tree structure.
    module Ancestry
      # Returns the root node
      def root
        return self if is_root?
        return parent.root
      end

      # Is this node the root node?
      def root?
        self.is_a?(PSD::Node::Root)
      end
      alias :is_root? :root?

      # Returns all ancestors in the path of this node. This
      # does NOT return the root node.
      def ancestors
        return [] if parent.nil? || parent.is_root?
        return parent.ancestors + [parent]
      end

      # Does this node have any children nodes?
      def has_children?
        children.length > 0
      end

      # Inverse of has_children?
      def childless?
        !has_children?
      end

      # Returns all sibling nodes including the current node. Can also
      # be thought of as all children of the parent of this node.
      def siblings
        return [] if parent.nil?
        parent.children
      end

      def next_sibling
        return nil if parent.nil?
        index = siblings.index(self)
        siblings[index + 1]
      end

      def prev_sibling
        return nil if parent.nil?
        index = siblings.index(self)
        siblings[index - 1]
      end

      # Does this node have any siblings?
      def has_siblings?
        siblings.length > 1
      end

      # Is this node the only descendant of its parent?
      def only_child?
        siblings.length == 1
      end

      # Recursively get all descendant nodes, not including this node.
      def descendants
        children.map(&:subtree).flatten
      end

      # Same as descendants, except it includes this node.
      def subtree
        [self] + descendants
      end

      # Depth from the root node. Root depth is 0.
      def depth
        return ancestors.length + 1
      end

      def path
        (ancestors.map(&:name) + [name]).join('/')
      end

      def method_missing(method, *args, &block)
        test = /^(.+)_(layers|groups)$/.match(method)
        if test
          m = self.respond_to?(test[1]) ? test[1] : "#{test[1]}s"
          self.send(m).select &method("#{test[2]}_only")
        else
          super
        end
      end

      private

      def layers_only(d); d.is_a?(PSD::Node::Layer); end
      def groups_only(d); d.is_a?(PSD::Node::Group); end
    end
  end
end