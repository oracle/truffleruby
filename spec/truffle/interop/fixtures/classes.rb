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

    def polyglot_address
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

    def polyglot_array?
      @log << [__callee__]
      true
    end

    def polyglot_array_size
      @log << [__callee__]
      @storage.size
    end

    def polyglot_array_read(index)
      @log << [__callee__, index]
      @storage[index]
    end

    def polyglot_array_write(index, value)
      @log << [__callee__, index, value]
      @storage[index] = value
      nil
    end

    def polyglot_array_remove(index)
      @log << [__callee__, index]
      @storage.delete_at(index)
      nil
    end

    def polyglot_array_readable?(index)
      @log << [__callee__, index]
      index >= 0 && index < @storage.size
    end

    def polyglot_array_modifiable?(index)
      @log << [__callee__, index]
      index >= 0 && index < @storage.size
    end

    def polyglot_array_insertable?(index)
      @log << [__callee__, index]
      #  ignoring that it should fit into int
      index >= @storage.size
    end

    def polyglot_array_removable?(index)
      @log << [__callee__, index]
      index >= 0 && index < @storage.size
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
