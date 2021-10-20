# dummy implementation

def foo(x, y)
  (x..y)
end

foo(1, 2)

__END__
# Classes
class Object
  private
  def foo: (Integer x, Integer y) -> Range
end
