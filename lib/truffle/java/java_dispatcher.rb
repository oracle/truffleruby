module JavaUtilities
  class JavaDispatcher

    attr_reader :methods
    
    def initialize(a_method)
      @methods = [a_method]
    end

    def combine_with(a_dispatcher)
      @methods.push(*(a_dispatcher.methods))
    end

    def method_for_dispatch
      if @methods.size == 1
        lambda { |*args|
          args = args.map do |x|
            ::JavaUtilities.unwrap_java_value(x)
          end
          ::JavaUtilities.wrap_java_value(Java.invoke_java_method(@methods[0], *args)) }
      else
        # Let's start building a dispatcher...
        # First sort the methods by arity and varargs.
        simple_arities = Hash.new { |h, k| h[k] = [] }
        varargs_arities = Hash.new { |h, k| h[k] = [] }
        @methods.each do |m|
          p m
          varargs = method_is_varargs(m)
          dest = if varargs
                   varargs_arities
                 else
                   simple_arities
                 end
          types = method_arg_types(m)
          min_ariyy = ::JavaUtilities.java_array_size(types)
          min_ariyy -= 1 if varargs
          bucket = dest[min_ariyy]
          bucket << m
        end
        lambda do |*args|
          args = args.map do |x|
            ::JavaUtilities.unwrap_java_value(x)
          end
          targets = []
          targets.push(*simple_arities[args.size])
          if targets.size == 1
            ::JavaUtilities.wrap_java_value(Java.invoke_java_method(targets[0], *args))
          else
            raise Exception, "Can't dispatch yet."
          end
        end
      end
    end

    private

    JAVA_METHODTYPE_CLASS = Java.java_class_by_name("java.lang.invoke.MethodType")
    METHODHANDLE_TYPE = Java.get_java_method(
      JAVA_METHODHANDLE_CLASS, "type", false, JAVA_METHODTYPE_CLASS);
    METHODHANDLE_VARARGS = Java.get_java_method(
      JAVA_METHODHANDLE_CLASS, "isVarargsCollector", false, JAVA_PRIM_BOOLEAN_CLASS);
    METHODTYPE_PARAMETERS = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "parameterArray", false, JAVA_CLASS_ARRAY)
    METHODTYPE_PARAM_COUNT = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "parameterCount", false, JAVA_PRIM_INT_CLASS)
    METHODTYPE_RETURN_TYPE = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "returnType", false, JAVA_CLASS_CLASS)
    
    def method_arg_types(m)
      Java.invoke_java_method(
        METHODTYPE_PARAMETERS,
        Java.invoke_java_method(METHODHANDLE_TYPE, m))
    end

    def method_is_varargs(m)
      Java.invoke_java_method(METHODHANDLE_VARARGS, m)
    end
    
    def method_return_type(m)
      Java.invoke_java_method(
        METHODTYPE_RETURN_TYPE,
        JAVA.invoke_java_method(METHODHANDLE_TYPE, m))
    end
    
  end
end
