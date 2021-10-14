Foo = Struct.new(:foo)

class Foo
  def initialize(foo)
    super(foo.to_s)
  end
end

Foo.new(42)

__END__
# Classes
class Foo < Struct[untyped]
  attr_accessor foo(): String
  def initialize: (Integer foo) -> Foo
end
