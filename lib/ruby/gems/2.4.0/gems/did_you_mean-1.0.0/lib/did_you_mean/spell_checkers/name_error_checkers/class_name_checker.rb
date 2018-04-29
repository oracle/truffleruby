# -*- frozen-string-literal: true -*-
require 'delegate'

module DidYouMean
  class ClassNameChecker
    include SpellCheckable
    attr_reader :class_name

    def initialize(exception)
      @class_name, @receiver = exception.name, exception.receiver
    end

    def candidates
      {class_name => class_names}
    end

    def class_names
      scopes.flat_map do |scope|
        scope.constants.map do |c|
          ClassName.new(c, scope == Object ? "" : "#{scope}::")
        end
      end
    end

    def corrections
      super.map(&:full_name)
    end

    def scopes
      @scopes ||= @receiver.to_s.split("::").inject([Object]) do |_scopes, scope|
        _scopes << _scopes.last.const_get(scope)
      end.uniq
    end

    class ClassName < SimpleDelegator
      attr :namespace

      def initialize(name, namespace = '')
        super(name)
        @namespace = namespace
      end

      def full_name
        self.class.new("#{namespace}#{__getobj__}")
      end
    end

    private_constant :ClassName
  end
end
