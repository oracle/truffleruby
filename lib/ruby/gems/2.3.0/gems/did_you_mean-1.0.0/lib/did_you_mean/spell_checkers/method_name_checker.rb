module DidYouMean
  class MethodNameChecker
    include SpellCheckable
    attr_reader :method_name, :receiver

    def initialize(exception)
      @method_name = exception.name
      @receiver    = exception.receiver
      @has_args    = !exception.args&.empty?
    end

    def candidates
      { method_name => method_names }
    end

    def method_names
      method_names = receiver.methods + receiver.singleton_methods
      method_names += receiver.private_methods if @has_args
      method_names.delete(method_name)
      method_names.uniq!
      method_names
    end
  end
end
