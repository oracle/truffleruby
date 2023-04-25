# frozen_string_literal: true

module RuboCop
  module Cop
    module TruffleRuby
      # Prefer `Primitive.true?` and `Primitive.false?` to check whether object
      # is `true` or `false`.
      #
      # @example
      #
      #   # bad
      #   foo == true
      #   foo == false
      #
      #   # bad
      #   foo.equal?(true)
      #   foo.equal?(false)
      #
      #   # bad
      #   Primitive.equal?(foo, true)
      #
      #   # good
      #   Primitive.true?(foo)
      #   Primitive.false?(foo)
      #
      class ReplaceWithPrimitiveTrueAndFalsePredicates < Base
        extend AutoCorrector

        MSG = 'Use `Primitive.true?` and `Primitive.false?` instead of `==` or `#equal?`'

        RESTRICT_ON_SEND = %i[== != equal?].freeze

        # @!method bad_core_method?(node)
        def_node_matcher :bad_core_method?, <<~PATTERN
          (send $_ ${ :== :!= :equal? } ${ true false })
        PATTERN

        # @!method bad_primitive_method?(node)
        def_node_matcher :bad_primitive_method?, <<~PATTERN
          (send
            (const {nil? cbase} :Primitive)
            :equal?
            {
              $_ ${ true false }
              |
              ${ true false } $_
            }
          )
        PATTERN

        def on_send(node)
          on_bad_core_method(node) or on_bad_primitive_method(node)
        end

        private

        def on_bad_core_method(node)
          captures = bad_core_method?(node)
          return unless captures

          add_offense(node) do |corrector|
            receiver, method, true_or_false = captures

            receiver_source = receiver&.source || 'self'
            optional_negation = method == :!= ? '!' : ''
            primitive_name = true_or_false.true_type? ? :true? : :false?

            source_string = "#{optional_negation}Primitive.#{primitive_name}(#{receiver_source})"
            corrector.replace(node.loc.expression, source_string)
          end
        end

        def on_bad_primitive_method(node)
          captures = bad_primitive_method?(node)
          return unless captures

          add_offense(node) do |corrector|
            true_or_false, object = captures[0].boolean_type? ? captures : captures.reverse
            primitive_name = true_or_false.true_type? ? :true? : :false?

            source_string = "Primitive.#{primitive_name}(#{object.source})"
            corrector.replace(node.loc.expression, source_string)
          end
        end
      end
    end
  end
end
