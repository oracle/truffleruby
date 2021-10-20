class Foo
  attr_reader :foo
  def initialize(foo)
    @foo = foo
  end
end

def log
  [Foo.new(42)].map(&:foo)
end

__END__
# Classes
class Object
  private
  def log: -> (Array[Integer | untyped])
end

class Foo
  attr_reader foo: Integer | untyped
  def initialize: (Integer | untyped foo) -> (Integer | untyped)
end
