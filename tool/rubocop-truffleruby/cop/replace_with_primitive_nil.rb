# frozen_string_literal: true

module RuboCop
  module Cop
    module TruffleRuby
      # Prefer Primitive method `nil?` to check whether object is `nil`
      #
      # @example
      #
      #   # bad
      #   object.nil?
      #
      #   # bad
      #   object == nil
      #
      #   # bad
      #   object != nil
      #
      #   # bad
      #   object.equal?(nil)
      #
      #   # bad
      #   nil.equal?(object)
      #
      #   # bad
      #   Primitive.object_equal(nil, object)
      #
      #   # bad
      #   Primitive.object_equal(object, nil)
      #
      #   # good
      #   Primitive.nil?(object)
      #
      class ReplaceWithPrimitiveNil < Base
        extend AutoCorrector

        MSG = 'Use `Primitive.nil?` instead of `Object#nil?` or `object == nil`'
        RESTRICT_ON_SEND = %i[nil? == != equal? object_equal].freeze

        # @!method bad_method?(node)
        def_node_matcher :bad_method?, <<~PATTERN
          {
            (send $_ :nil?)
            (send $_ :== nil)
            (send $_ :!= nil)
            (send nil :equal? $_)
            (send $_ :equal? nil)
            (send (const {nil? cbase} :Primitive) :object_equal $_ nil)
            (send (const {nil? cbase} :Primitive) :object_equal nil $_)
          }
        PATTERN

        def on_send(node)
          receiver = bad_method?(node)
          return unless receiver

          add_offense(node) do |corrector|
            source_string = "Primitive.nil?(#{receiver.source})"
            source_string = "!#{source_string}" if node.method_name == :!=
            corrector.replace(node.loc.expression, source_string)
          end
        end
      end
    end
  end
end
