def bar
  baz do
    yield
  end
end

def baz
  yield
end

def foo
  a = 42
  bar do
    a = "str"
  end
  a
end

foo

__END__
# Classes
class Object
  private
  def bar: { -> String } -> String
  def baz: { -> String } -> String
  def foo: -> (Integer | String)
end
