module JavaUtilities
  class JavaException < StandardError
    attr_reader :java_exception
    def initialize(java_exception)
      @java_exception = java_exception
    end
  end
end
