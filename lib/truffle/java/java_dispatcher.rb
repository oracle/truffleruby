module JavaUtilities
  class JavaDispatcher
    def initialize(a_method)
      @methods = [a_method]
    end

    def add_method(a_method)
      @methods << a_method
    end

    def method_for_dispatch
      if @methods.size == 1
        lambda { |*args|
          args = args.map do |x|
            ::JavaUtilities.unwrap_java_value(x)
          end
          ::JavaUtilities.wrap_java_value(Java.invoke_java_method(@methods[0], *args)) }
      else
        lambda { |*args| raise Exception, "Can't dispatch yet." }
      end
    end
  end
end
