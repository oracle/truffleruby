def foo(x)
  x
end

x = nil
foo(x || 1)

__END__
# Classes
class Object
  private
  def foo: (Integer x) -> Integer
end
