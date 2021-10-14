def foo(a)
  a.replace(["str"])
  a
end

foo([1, 2, 3])

__END__
# Classes
class Object
  private
  def foo: ([Integer, Integer, Integer] a) -> (Array[Integer | String])
end
