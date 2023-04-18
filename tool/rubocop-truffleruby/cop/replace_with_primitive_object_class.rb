# frozen_string_literal: true

module RuboCop
  module Cop
    module TruffleRuby
      # Prefer `Primitive.class` to get a logical class of an object.
      #
      # @example
      #
      #   # bad
      #   object.class
      #
      #   # good
      #   Primitive.class(object)
      #
      class ReplaceWithPrimitiveObjectClass < Base
        extend AutoCorrector

        MSG = 'Use `Primitive.class` instead of `Object#class`'
        RESTRICT_ON_SEND = %i[class].freeze

        # @!method bad_method?(node)
        def_node_matcher :bad_method?, <<~PATTERN
          (send $_ :class)
        PATTERN

        def on_send(node)
          receiver = bad_method?(node)
          return unless receiver

          add_offense(node) do |corrector|
            source_string = "Primitive.class(#{receiver.source})"
            corrector.replace(node.loc.expression, source_string)
          end
        end
      end
    end
  end
end
