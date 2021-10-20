def bar(x)
end

def foo(x)
  x.tap {|n|
    bar(n)
  }
end

foo(1)

__END__
# Classes
class Object
  private
  def bar: (Integer x) -> nil
  def foo: (Integer x) -> Integer
end
