def bar(x)
  x
end

def foo(x)
  bar(x)
rescue
  x = "str"
  retry
  42
end

foo(42)

__END__
# Classes
class Object
  private
  def bar: (Integer | String x) -> (Integer | String)
  def foo: (Integer | String x) -> (Integer | String)
end
