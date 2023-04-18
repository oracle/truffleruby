# frozen_string_literal: true

module RuboCop
  module Cop
    module TruffleRuby
      # Prefer Primitive method `is_a?` to check an object class for
      # performance reasons.
      #
      # @safety
      #   This cop is unsafe because #=== call's receiver might be not a module.
      #
      # @example
      #
      #   # bad
      #   a.is_a?(String)
      #
      #   # bad
      #   a.kind_of?(String)
      #
      #   # bad
      #   String === a
      #
      #   # good
      #   Primitive.is_a?(a, String)
      #
      class ReplaceWithPrimitiveObjectKindOf < Base
        extend AutoCorrector

        MSG = 'Use `Primitive.is_a?` instead of `#kind_of?` or `#is_a?`'
        RESTRICT_ON_SEND = %i[is_a? kind_of? ===].freeze

        # @!method bad_method?(node)
        def_node_matcher :bad_method?, <<~PATTERN
          {
            (send $_ { :is_a? :kind_of? } $_)
            (send $const :=== $_)
          }
        PATTERN

        def on_send(node)
          captures = bad_method?(node)
          return unless captures

          add_offense(node) do |corrector|
            source_string = build_expression_to_replace_by(node, captures)
            corrector.replace(node.loc.expression, source_string)
          end
        end

        private

        def build_expression_to_replace_by(node, captures)
          if node.method_name == :===
            object = captures[1].source
            constant = captures[0].source
          else
            object = captures[0]&.source || 'self'
            constant = captures[1].source
          end

          "Primitive.is_a?(#{object}, #{constant})"
        end
      end
    end
  end
end
