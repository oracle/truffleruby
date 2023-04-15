# frozen_string_literal: true

module RuboCop
  module Cop
    module TruffleRuby
      # Prefer Primitive method `object_equal` to check whether two objects are
      # the same object.
      #
      # @example
      #
      #   # bad
      #   foo.equal?(bar)
      #
      #   # good
      #   Primitive.object_equal(foo, bar)
      #
      class ReplaceWithPrimitiveObjectEqual < Base
        extend AutoCorrector

        MSG = 'Use `Primitive.object_equal` instead of `#equal?`'
        RESTRICT_ON_SEND = %i[equal?].freeze

        # @!method bad_method?(node)
        def_node_matcher :bad_method?, <<~PATTERN
          (send $_ :equal? $_)
        PATTERN

        def on_send(node)
          captures = bad_method?(node)
          return unless captures

          add_offense(node) do |corrector|
            receiver, argument = captures.map { |n| n&.source }
            receiver ||= 'self'

            source_string = "Primitive.object_equal(#{receiver}, #{argument})"
            corrector.replace(node.loc.expression, source_string)
          end
        end
      end
    end
  end
end
