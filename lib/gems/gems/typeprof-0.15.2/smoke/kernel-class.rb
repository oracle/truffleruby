def foo(n)
  n.class
end

foo(1)
foo("")

__END__
# Classes
class Object
  private
  def foo: (Integer | String n) -> (singleton(Integer) | singleton(String))
end
