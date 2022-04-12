def foo(a)
  a.map {|n| n.to_s }
end

foo([1, 2, 3])

__END__
# Classes
class Object
  private
  def foo: ([Integer, Integer, Integer] a) -> Array[String]
end
