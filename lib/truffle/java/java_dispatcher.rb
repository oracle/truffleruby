module JavaUtilities
  class JavaDispatcher

    include Truffle::Interop::Java

    attr_reader :methods

    def initialize(a_method)
      @methods = [a_method]
    end

    def combine_with(a_dispatcher)
      @methods.push(*(a_dispatcher.methods))
    end

    def method_for_dispatch(replacer)
      if @methods.size == 1
        replacer.call(
          lambda { |*args|
            args = args.map do |x|
              ::JavaUtilities.unwrap_java_value(x)
            end
            ::JavaUtilities.wrap_java_value(Java.invoke_java_method(@methods[0], *args)) }
        )
      else
        # Let's start building a dispatcher...
        # First sort the methods by arity and varargs.
        simple_arities = Hash.new { |h, k| h[k] = [] }
        varargs_arities = Hash.new { |h, k| h[k] = [] }
        @methods.each do |m|
          varargs = JavaDispatcher.method_is_varargs(m)
          dest = if varargs
                   varargs_arities
                 else
                   simple_arities
                 end
          bucket = dest[min_method_arity(m)]
          bucket << m
        end
        simple_arities = simple_arities.map { |k, methods| [k, CallableSelector.new(methods)] }.to_h
        method = lambda do |*args|
          cs = simple_arities[args.size]
          target = cs.find_matching_callable_for_args( args )
          java_args = args.map { |x| ::JavaUtilities.unwrap_java_value(x) }
          ::JavaUtilities.wrap_java_value(Java.invoke_java_method(target, *java_args))
        end
        replacer.call(method)
      end
    end

    def basic_arity
      raise Exception, "Can only give basic arity for single Java target." if @methods.size != 1
      m = @methods[0]
      arity = Java.invoke_java_method(
        METHODTYPE_PARAM_COUNT,
        Java.invoke_java_method(METHODHANDLE_TYPE, m))
      arity = -arity if JavaDispatcher.method_is_varargs(m)
      arity
    end

    def self.method_is_varargs(m)
      Java.invoke_java_method(METHODHANDLE_VARARGS, m)
    end

    def self.method_types(m)
      types_java = Java.invoke_java_method(
        METHODTYPE_PARAMETERS,
        Java.invoke_java_method(METHODHANDLE_TYPE, m))
      types_size = JavaUtilities.java_array_size(types_java)
      types_array = Array.new(types_size)
      (0...types_size).each { |i| types_array[i] = JavaUtilities.java_array_get(types_java, i) }
      types_array
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

    def min_method_arity(m)
      arity = Java.invoke_java_method(
        METHODTYPE_PARAM_COUNT,
        Java.invoke_java_method(METHODHANDLE_TYPE, m))
      arity -= 1 if JavaDispatcher.method_is_varargs(m)
      arity
    end

    def arg_types(args)
      args.map do |x|
        case x
        when Fixnum
          :fixnum
        when Float
          :float
        when true
          :boolean
        when false
          :boolean
        else
          JavaUtilities.get_java_class(x)
        end
      end
    end

    def self.method_return_type(m)
      Java.invoke_java_method(
        METHODTYPE_RETURN_TYPE,
        JAVA.invoke_java_method(METHODHANDLE_TYPE, m))
    end

    # Primitive and boxes types needed for checking compatibility

    JAVA_INTEGER_CLASS = Java.java_class_by_name("java.lang.Integer")
    JAVA_DOUBLE_CLASS = Java.java_class_by_name("java.lang.Double")

    def self.java_type_compatible(method_type, arg_type)
      if :fixnum == arg_type
        Java.java_refs_equal?(method_type, JavaUtilities::JAVA_OBJECT_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_INTEGER_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_LONG_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_PRIM_INT_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_PRIM_LONG_CLASS)
      elsif :float == arg_type
        Java.java_refs_equal?(method_type, JavaUtilities::JAVA_OBJECT_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_DOUBLE_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_DBL_CLASS)
      elsif :boolean == arg_type
        Java.java_refs_equal?(method_type, JavaUtilities::JAVA_OBJECT_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_BOOLEAN_CLASS) ||
          Java.java_refs_equal?(method_type, JavaUtilities::JAVA_PRIM_BOOLEAN_CLASS)        
      else
        Java.invoke_java_method(
          CLASS_IS_ASSIGNABLE_FROM, method_type, arg_type)
      end
    end

    def self.types_compatible(method_types, arg_types)
      arg_types.zip(method_types).reduce(true) do |res, x|
        res && java_type_compatible(x[1], x[0])
      end
    end

  end
end
