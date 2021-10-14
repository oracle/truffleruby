CONST = 1

class Foo
  CONST = "str"
  class Bar
    def get1
      CONST
    end

    def get2
      ::CONST
    end

    def get3
      Object::CONST
    end
  end
end

class Foo::Bar
  def get4
    CONST
  end
end

Foo::Bar.new.get1 # String
Foo::Bar.new.get2 # Integer
Foo::Bar.new.get3 # Integer
Foo::Bar.new.get4 # Integer

__END__
# Classes
class Object
  CONST: Integer
end

class Foo
  CONST: String

  class Bar
    def get1: -> String
    def get2: -> Integer
    def get3: -> Integer
    def get4: -> Integer
  end
end
