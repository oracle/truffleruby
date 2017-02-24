# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

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
        check = target.checker(args)
        conn = target.converter(args)
        check[args]
        conn[args]

        ::JavaUtilities.wrap_java_value(Java.invoke_java_method(target.mh, *args))
      end
      replacer.call(method)
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
      JAVA_METHODHANDLE_CLASS, "isVarargsCollector", false, JAVA_PRIM_BOOLEAN_CLASS)
    METHODHANDLE_AS_VARARGS = Java.get_java_method(
      JAVA_METHODHANDLE_CLASS, "asVarargsCollector", false, JAVA_METHODHANDLE_CLASS, JAVA_CLASS_CLASS)
    METHODHANDLE_AS_TYPE = Java.get_java_method(
        JAVA_METHODHANDLE_CLASS, "asType", false, JAVA_METHODHANDLE_CLASS, JAVA_METHODTYPE_CLASS)
    METHODHANDLES_CAST_ARGS = Java.get_java_method(
      JAVA_METHODHANDLES_CLASS, "explicitCastArguments", true,
      JAVA_METHODHANDLE_CLASS, JAVA_METHODHANDLE_CLASS, JAVA_METHODTYPE_CLASS)
    METHODTYPE_METHODTYPE = Java.invoke_java_method(
      METHODHANDLE_AS_VARARGS,
      Java.get_java_method(
        JAVA_METHODTYPE_CLASS, "methodType", true, JAVA_METHODTYPE_CLASS, JAVA_CLASS_CLASS, JAVA_CLASS_ARRAY),
      JAVA_CLASS_ARRAY)
    METHODTYPE_PARAMETERS = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "parameterArray", false, JAVA_CLASS_ARRAY)
    METHODTYPE_PARAM_COUNT = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "parameterCount", false, JAVA_PRIM_INT_CLASS)
    METHODTYPE_RETURN_TYPE = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "returnType", false, JAVA_CLASS_CLASS)
    METHODTYPE_UNWRAP = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "unwrap", false, JAVA_METHODTYPE_CLASS)
    METHODTYPE_WRAP = Java.get_java_method(
      JAVA_METHODTYPE_CLASS, "wrap", false, JAVA_METHODTYPE_CLASS)
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
        Java.invoke_java_method(METHODHANDLE_TYPE, m))
    end

    def self.method_exact_cast(m, return_type, param_types)
      Java.invoke_java_method()
    end

    def self.widen_method(m)
      return_type = JavaDispatcher::method_return_type(m)
      mt = Java.invoke_java_method(METHODHANDLE_TYPE, m)
      uwmt = Java.invoke_java_method(METHODTYPE_UNWRAP, mt)
      m = Java.invoke_java_method(METHODHANDLE_AS_TYPE, m, uwmt)
      params = Java.invoke_java_method(METHODTYPE_PARAMETERS, uwmt)
      rt = Java.invoke_java_method(METHODTYPE_RETURN_TYPE, uwmt)
      params_size = JavaUtilities.java_array_size(params)
      new_params = Array.new(params_size)
      (0...params_size).each do |i|
        p = JavaUtilities.java_array_get(params, i)
        wt = WideningType::PrimitiveTypes[p]
        if wt != nil
          new_params[i] = wt.wide_type.value_class
        else
          new_params[i] = p
        end
      end
      new_mt = Java.invoke_java_method(METHODTYPE_METHODTYPE, rt, *new_params)
      Java.invoke_java_method(METHODHANDLES_CAST_ARGS, m, new_mt)
    end
  end
end
