def foo(x)
  x
end

def bar(x)
  x
end

def dispatch(mid)
  send(mid, 1)
end

dispatch(:foo)
dispatch(:bar)

__END__
# Classes
class Object
  private
  def foo: (Integer x) -> Integer
  def bar: (Integer x) -> Integer
  def dispatch: (:bar | :foo mid) -> Integer
end
