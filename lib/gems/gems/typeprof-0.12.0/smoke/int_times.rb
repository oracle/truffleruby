def foo(n)
  z = "str"
  n.times {|i| z = i }
  n.times { z = 42 }
  z
end

foo(42)

__END__
# Classes
class Object
  private
  def foo: (Integer n) -> (Integer | String)
end
