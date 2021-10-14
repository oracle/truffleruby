def foo(a, *r, z)
  r
end

foo(1, 2, "S", 3)
foo(1, 2, 3)
foo("a", "b", "c")
foo(:a, :z)

def bar(a, o=1, *r, z)
  r
end

bar("A", "Z")
bar("A", "B", "Z")
bar("A", "B", "C", "Z")
bar("A", "B", "C", "D", "Z")
bar("A", "B", "C", "D", "E", "Z")

__END__
# Classes
class Object
  private
  def foo: (:a | Integer | String a, *Integer | String r, :z | Integer | String z) -> (Array[Integer | String])
  def bar: (String a, ?Integer | String o, *String r, String z) -> Array[String]
end
