def foo(x)
  x.is_a?(Integer) ? x : x
  1
end
foo(unknown)

__END__
# Errors
smoke/flow2.rb:5: [error] undefined method: Object#unknown

# Classes
class Object
  private
  def foo: (untyped x) -> Integer
end
