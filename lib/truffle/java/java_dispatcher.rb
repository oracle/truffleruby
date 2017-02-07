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
        # Let's start building a dispatcher...
        lambda { |*args| raise Exception, "Can't dispatch yet." }
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
        JAVA.invoke_java_method(METHODHANDLE_TYPE, m))
    end

    def method_is_varargs
      Java.invoke_java_method(METHODHANDLE_VARARGS, m)
    end
    
    def method_return_type
      Java.invoke_java_method(
        METHODTYPE_RETURN_TYPE,
        JAVA.invoke_java_method(METHODHANDLE_TYPE, m))
    end
    
  end
end
