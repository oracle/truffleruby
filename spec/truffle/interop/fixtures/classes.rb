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
