# frozen_string_literal: true

module RuboCop
  module Cop
    module TruffleRuby
      # Prefer Primitive method `object_class` to get a logical class of an object.
      #
      # @example
      #
      #   # bad
      #   object.class
      #
      #   # good
      #   Primitive.object_class(object)
      #
      class ReplaceWithPrimitiveObjectClass < Base
        extend AutoCorrector

        MSG = 'Use `Primitive.object_class` instead of `Object#class`'
        RESTRICT_ON_SEND = %i[class].freeze

        # @!method bad_method?(node)
        def_node_matcher :bad_method?, <<~PATTERN
          (send $_ :class)
        PATTERN

        def on_send(node)
          receiver = bad_method?(node)
          return unless receiver

          add_offense(node) do |corrector|
            source_string = "Primitive.object_class(#{receiver.source})"
            corrector.replace(node.loc.expression, source_string)
          end
        end
      end
    end
  end
end
