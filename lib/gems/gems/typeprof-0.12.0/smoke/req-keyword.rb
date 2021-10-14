def foo(a:)
  foo(a: a + 1) if rand < 0.5
  a
end

foo(a:1)

__END__
# Classes
class Object
  private
  def foo: (a: Integer) -> Integer
end
