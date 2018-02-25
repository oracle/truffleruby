module TruffleInteropSpecs

  class AsPointerClass
    def address
      0x123
    end
  end

  class InvokeTestClass
    def add(a, b)
      a + b
    end
  end

  class KeyInfoFixture
    def initialize
      @exists = 14
    end

    attr_reader :ro
    attr_accessor :rw
    attr_writer :wo
  end

  class InteropKeysClass
    def initialize
      @a = 1
      @b = 2
      @c = 3
    end
  end

  class NewTestClass
    attr_reader :x

    def initialize(a, b)
      @x = a + b
    end
  end

  class ReadInstanceVariable
    def initialize
      @foo = 14
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
    attr_reader :key

    def []=(n, value)
      @key = n
      @called = true
    end

    def called?
      @called
    end
  end

end
