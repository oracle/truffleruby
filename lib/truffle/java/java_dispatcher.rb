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
          varargs = method_is_varargs(m)
          dest = if varargs
                   varargs_arities
                 else
                   simple_arities
                 end
          bucket = dest[min_method_arity(m)]
          bucket << m
        end
        lambda do |*args|
          java_args = args.map { |x| ::JavaUtilities.unwrap_java_value(x) }
          targets = []
          targets.push(*simple_arities[args.size])
          start_size = targets.size
          targets = targets.select { |m| types_compatible( method_types(m), arg_types(java_args)) }
          filter_size = targets.size
          case targets.size
          when 0
            raise TypeError, "No java method found that accepts #{args}."
          when 1
            ::JavaUtilities.wrap_java_value(Java.invoke_java_method(targets[0], *java_args))
          else
            raise TypeError, "Can't dispatch ambiguous cases yet."
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
    CLASS_IS_ASSIGNABLE_FROM = Java.get_java_method(
      JAVA_CLASS_CLASS, "isAssignableFrom", false, JAVA_PRIM_BOOLEAN_CLASS, JAVA_CLASS_CLASS)
    
    def method_types(m)
      types_java = Java.invoke_java_method(
        METHODTYPE_PARAMETERS,
        Java.invoke_java_method(METHODHANDLE_TYPE, m))
      types_size = JavaUtilities.java_array_size(types_java)
      types_array = Array.new(types_size)
      (0...types_size).each { |i| types_array[i] = JavaUtilities.java_array_get(types_java, i) }
      types_array
    end

    def min_method_arity(m)
      arity = Java.invoke_java_method(
        METHODTYPE_PARAM_COUNT,
        Java.invoke_java_method(METHODHANDLE_TYPE, m))
      arity -= 1 if method_is_varargs(m)
      arity
    end
      
    def arg_types(args)
      args.map { |x| JavaUtilities.get_java_class(x) }
    end
                 
    def method_is_varargs(m)
      Java.invoke_java_method(METHODHANDLE_VARARGS, m)
    end
    
    def method_return_type(m)
      Java.invoke_java_method(
        METHODTYPE_RETURN_TYPE,
        JAVA.invoke_java_method(METHODHANDLE_TYPE, m))
    end

    def java_type_compatible(method_type, arg_type)
      Java.invoke_java_method(
        CLASS_IS_ASSIGNABLE_FROM, method_type, arg_type)
    end

    def types_compatible(method_types, arg_types)
      arg_types.zip(method_types).reduce(true) do |res, x|
        res && java_type_compatible(x[1], x[0])
      end        
    end
  end
end

