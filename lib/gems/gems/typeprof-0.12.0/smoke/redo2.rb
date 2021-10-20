def bar(x)
  x
end

def foo
  yield 42
end

foo do |x|
  bar(x)
rescue
  x = "str"
  redo if rand < 0.5
  42
end

__END__
# Classes
class Object
  private
  def bar: (Integer | String x) -> (Integer | String)
  def foo: { (Integer) -> (Integer | String) } -> (Integer | String)
end
