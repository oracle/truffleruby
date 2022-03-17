def foo(x); end

foo(Integer(1))
foo(Integer("str"))

__END__
# Classes
class Object
  private
  def foo: (Integer x) -> nil
end
