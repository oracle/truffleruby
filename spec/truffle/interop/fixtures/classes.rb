# truffleruby_primitives: true

# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module TruffleInteropSpecs

  class AsPointerClass
    def polyglot_pointer?
      true
    end

    def polyglot_as_pointer
      0x123
    end
  end

  class InvokeTestClass
    def add(a, b)
      a + b
    end
  end

  class InteropKeysClass
    def initialize
      @a = 1
      @b = 2
      @c = 3
    end

    def foo
      14
    end
  end

  class InteropKeysIndexClass
    def initialize
      @a = 1
      @b = 2
      @c = 3
    end

    def [](n)
      instance_variable_get(:"@#{n}")
    end

    def foo
      14
    end
  end

  class NewTestClass
    attr_reader :x

    def initialize(a, b)
      @x = a + b
    end
  end

  class ReadHasMethod
    def foo
      14
    end
  end

  class PolyglotArray
    attr_reader :log

    def initialize
      @log = []
      @storage = []
    end

    def polyglot_has_array_elements?
      @log << [__callee__]
      true
    end

    def polyglot_array_size
      @log << [__callee__]
      @storage.size
    end

    def polyglot_read_array_element(index)
      @log << [__callee__, index]
      @storage[index]
    end

    def polyglot_write_array_element(index, value)
      @log << [__callee__, index, value]
      @storage[index] = value
      nil
    end

    def polyglot_remove_array_element(index)
      @log << [__callee__, index]
      @storage.delete_at(index)
      nil
    end

    def polyglot_array_element_readable?(index)
      @log << [__callee__, index]
      index >= 0 && index < @storage.size
    end

    def polyglot_array_element_modifiable?(index)
      @log << [__callee__, index]
      index >= 0 && index < @storage.size
    end

    def polyglot_array_element_insertable?(index)
      @log << [__callee__, index]
      index >= @storage.size && Primitive.integer_fits_into_int(index)
    end

    def polyglot_array_element_removable?(index)
      @log << [__callee__, index]
      index >= 0 && index < @storage.size
    end
  end

  class PolyglotMember
    attr_reader :log

    def initialize
      @log = []
      @storage = {}
    end

    def polyglot_has_members?
      true
    end

    def polyglot_members(internal)
      @log << [__callee__, internal]
      internal ? [] : @storage.keys
    end

    def polyglot_read_member(name)
      @log << [__callee__, name]
      @storage.fetch name # TODO (pitr-ch 07-Feb-2020): error translation for missing keys?
    end

    def polyglot_write_member(name, value)
      @log << [__callee__, name, value]
      @storage[name] = value
    end

    def polyglot_remove_member(name)
      @log << [__callee__, name]
      @storage.delete name # TODO (pitr-ch 07-Feb-2020): error translation for missing keys?
    end

    def polyglot_invoke_member(name, *args)
      @log << [__callee__, name, *args]
      # TODO (pitr-ch 07-Feb-2020): error handling?
      @storage.fetch(name).call(*args)
    end

    def polyglot_member_readable?(name)
      @log << [__callee__, name]
      @storage.key? name
    end

    def polyglot_member_modifiable?(name)
      @log << [__callee__, name]
      @storage.key? name
    end

    def polyglot_member_removable?(name)
      @log << [__callee__, name]
      @storage.key? name
    end

    def polyglot_member_insertable?(name)
      @log << [__callee__, name]
      !@storage.key? name
    end

    def polyglot_member_invocable?(name)
      @log << [__callee__, name]
      @storage.key?(name) && Truffle::Interop.executable?(@storage.fetch(name))
    end

    def polyglot_member_internal?(name)
      false
    end

    def polyglot_has_member_read_side_effects?(name)
      false
    end

    def polyglot_has_member_write_side_effects?(name)
      false
    end
  end

  class ReadHasIndex
    attr_reader :key

    def [](n)
      @key = n
      14
    end

    def bob
      14
    end
  end

  class WriteHasMethod
    def foo=(value)
      @called = true
    end

    def called?
      @called
    end
  end

  class WriteHasIndexSet
    attr_reader :key, :value, :called

    def []=(n, value)
      @key = n
      @value = value
      @called = true
    end

    def bob
      14
    end
  end

  class WriteHasIndexSetAndIndex
    attr_reader :key, :value, :called

    def [](n)
      @value = n
    end

    def []=(n, value)
      @key = n
      @value = value
      @called = true
    end

    def bob
      14
    end
  end

end
