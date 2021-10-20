def foo(n)
  n.to_s
end

foo(42)

__END__
# Classes
class Object
  private
  def foo: (Integer n) -> String
end
