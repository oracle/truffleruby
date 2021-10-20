def foo(a, o1=1, o2=2, *r, z)
  [a, o1, o2, r, z]
end

foo("A", "Z")
foo("A", "B", "Z")
foo("A", "B", "C", "Z")
foo("A", "B", "C", "D", "Z")
foo("A", "B", "C", "D", "E", "Z")

__END__
# Classes
class Object
  private
  def foo: (String a, ?Integer | String o1, ?Integer | String o2, *String r, String z) -> ([String, Integer | String, Integer | String, Array[String], String])
end
