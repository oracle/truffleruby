def foo(x)
  yield 42
end

s = "str"
foo(1) do |x|
  s
end

__END__
# Classes
class Object
  private
  def foo: (Integer x) { (Integer) -> String } -> String
end
