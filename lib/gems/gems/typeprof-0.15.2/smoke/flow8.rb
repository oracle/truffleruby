def foo(n)
  n = n || 42
  n
end

foo(1)
foo(nil)

__END__
# Classes
class Object
  private
  def foo: (Integer? n) -> Integer
end
