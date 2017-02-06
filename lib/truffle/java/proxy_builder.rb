module JavaUtilities
  class JavaProxyBuilder

    def initialize(a_proxy, java_class)
      @java_class = java_class
      @proxy = a_proxy
      @static_members = {}
      @instance_members = {}
    end

    def add_to_singleton(name, thing)
      add_to_map(@static_members, name, thing)
    end
    
    def add_to_instance(name, thing)
      add_to_map(@instance_members, name, thing)
    end

    def add_to_map(a_map, name, thing)
      return a_map[name] = thing if ! a_map.has_key?(name)
      return a_map[name] = thing if a_map[name].precedence < thing.precedence
      return a_map[name].combine_with(thing) if a_map[name].precedence == thing.precedence
      return a_map[name]
    end

    def add_interfaces
      interfaces = Java.invoke_java_method(CLASS_GET_INTERFACES, @java_class)
      # Not using idiomatic Ruby here as we might not have bootstrapped that at this point.
      interfaces_size = JavaUtilities.java_array_size(interfaces)
      (0...interfaces_size).each do |i|
        interface_proxy = JavaUtilities.make_proxy(JavaUtilities.java_array_get(interfaces, i))
        @proxy.include(interface_proxy) unless @proxy.ancestors.include?(interface_proxy)
      end
      self
    end

    def add_static_fields
      fields = Java.invoke_java_method(CLASS_GET_DECLARED_FIELDS, @java_class)
      # Not using idiomatic Ruby here as we might not have bootstrapped that at this point.
      fields_size = JavaUtilities.java_array_size(fields)
      (0...fields_size).each do |i; f, mh, name, getter, setter, is_static, is_const, const_val|
        f = JavaUtilities.java_array_get(fields, i)
        mh = JavaUtilities.unreflect_getter(f)
        if mh != nil
          is_static = JavaUtilities.static_field?(f)
          next if !is_static
          name = Java.to_ruby_string(Java.invoke_java_method(FIELD_GET_NAME, f))
          is_const = JavaUtilities.constant_field?(f)
          cpmst_val = ::JavaUtilities.wrap_java_value(Java.invoke_java_method(mh)) if is_const
          getter = lambda { ::JavaUtilities.wrap_java_value(Java.invoke_java_method(mh)) }
          if !JavaUtilities.final_field?(f)
            mh = JavaUtilities.unreflect_getter(f)
            setter = lambda { |v|
              ::JavaUtilities.wrap_java_value(Java.invoke_java_method(mh, v)) }
          end
          add_to_singleton(name, Field.new(name, getter, setter, is_const, const_val))
        end
      end
      self
    end

    def add_instance_fields
      fields = Java.invoke_java_method(CLASS_GET_DECLARED_FIELDS, @java_class)
      # Not using idiomatic Ruby here as we might not have bootstrapped that at this point.
      fields_size = JavaUtilities.java_array_size(fields)
      (0...fields_size).each do |i; f, mh, name, getter, setter, is_static, is_const, const_val|
        f = JavaUtilities.java_array_get(fields, i)
        mh = JavaUtilities.unreflect_getter(f)
        if mh != nil
          is_static = JavaUtilities.static_field?(f)
          next if is_static
          name = Java.to_ruby_string(Java.invoke_java_method(FIELD_GET_NAME, f))
          getter = lambda {
            ::JavaUtilities.wrap_java_value(Java.invoke_java_method(mh, java_object)) }
          if !JavaUtilities.final_field?(f)
            mh = JavaUtilities.unreflect_getter(f)
            setter = lambda { |v|
              ::JavaUtilities.wrap_java_value(Java.invoke_java_method(mh, java_object, v)) }
          end
          add_to_instance(name, Field.new(name, getter, setter))
        end
      end
      self
    end

    def add_static_methods
      methods = Java.invoke_java_method(CLASS_GET_DECLARED_METHODS, @java_class)
      # Not using idiomatic Ruby here as we might not have bootstrapped that at this point.
      methods_size = JavaUtilities.java_array_size(methods)
      (0...methods_size).each do |i; m|
        m = JavaUtilities.java_array_get(methods, i)
        if JavaUtilities.static_method?(m)
          mh = JavaUtilities.unreflect_method(m)
          if mh != nil
            name = Java.to_ruby_string(Java.invoke_java_method(METHOD_GET_NAME, m))
            add_to_singleton(name, Method.new(name, mh))
          end
        end
      end
      self
    end    

    def add_instance_methods
      methods = Java.invoke_java_method(CLASS_GET_DECLARED_METHODS, @proxy.java_class)
      # Not using idiomatic Ruby here as we might not have bootstrapped that at this point.
      methods_size = JavaUtilities.java_array_size(methods)
      (0...methods_size).each do |i; m|
        m = JavaUtilities.java_array_get(methods, i)
        if !JavaUtilities.static_method?(m)
          mh = JavaUtilities.unreflect_method(m)
          if mh != nil
            name = Java.to_ruby_string(Java.invoke_java_method(METHOD_GET_NAME, m))
            add_to_instance(name, Method.new(name, mh))
          end
        end
      end
      self
    end

    def build
      @static_members.values.each do |m|
        m.add_to_proxy( @proxy, true )
      end
      @instance_members.values.each do |m|
        m.add_to_proxy( @proxy, false )
      end
    end
    @proxy
  end

  class Field
    def initialize(name, getter, setter, is_const=false, const_val=nil)
      @name, @getter, @setter, @is_const, @const_val =
          name, getter, setter, is_const, const_val
    end
    
    def precedence
      2
    end

    def add_to_proxy(a_proxy, static)
      if @is_const
        begin
          a_proxy.const_set(Java.to_ruby_string(Java.invoke_java_method(FIELD_GET_NAME, f)), val)
        rescue NameError
        end
      end
      message = if static
                  :define_singleton_method
                else
                  :define_method
                end
      a_proxy.__send__(message, @name, @getter)
      a_proxy.__send__(message, @name, @setter) if @xssetter != nil
    end

    def combine_with(a_field)
      raise Exception, "We should never see overloaded fields."
    end
  end
  
  class Method
    def initialize(name, a_method)
      @name = name
      @method = JavaDispatcher.new(a_method)
    end
    
    def precedence
      1
    end

    def add_to_proxy(a_proxy, static)
      message = if static
                  :define_singleton_method
                else
                  :define_method
                end
      method = @method.method_for_dispatch
      wrapped = if static
                  lambda { |*args| method[*args] }
                else 
                  lambda { |*args| method[self, *args] }
                end
      a_proxy.__send__(message, @name, wrapped)
    end

    def combine_with(a_method)
      @method.add_method(a_method)
    end
  end
end
