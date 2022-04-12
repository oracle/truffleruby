def foo
  [1, :sym, "str"].map(&:to_s)
end

class Foo
  def foo
    :foo
  end
end

def bar
  [Foo.new].map(&:foo)
end

__END__
# Classes
class Object
  private
  def foo: -> Array[String]
  def bar: -> Array[:foo]
end

class Foo
  def foo: -> :foo
end
