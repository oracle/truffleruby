def foo(x)
end
def bar(x)
end

bar(foo(1, 2))

__END__
# Errors
smoke/arguments.rb:6: [error] wrong number of arguments (given 2, expected 1)

# Classes
class Object
  private
  def foo: (untyped x) -> nil
  def bar: (untyped x) -> nil
end
