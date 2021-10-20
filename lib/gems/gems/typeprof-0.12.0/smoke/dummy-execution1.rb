def foo(n)
  n
end

def bar
  foo(1)
end

__END__
# Classes
class Object
  private
  def foo: (Integer | untyped n) -> (Integer | untyped)
  def bar: -> (Integer | untyped)
end
