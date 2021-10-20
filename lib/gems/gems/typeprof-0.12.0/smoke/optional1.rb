def foo(a, o1=1, o2=2, z)
  [a, o1, o2, z]
end

foo("A", "Z")
foo("A", "B", "Z")
foo("A", "B", "C", "Z")

__END__
# Classes
class Object
  private
  def foo: (String a, ?Integer | String o1, ?Integer | String o2, String z) -> ([String, Integer | String, Integer | String, String])
end
