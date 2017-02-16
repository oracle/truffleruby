module JavaUtilities
  class JavaException < StandardError
    attr_reader :java_exception
    def initialize(java_exception)
      @java_exception = java_exception
      message = java_exception.to_string rescue "A Java error occurred."
      super(message)
    end
  end
end
