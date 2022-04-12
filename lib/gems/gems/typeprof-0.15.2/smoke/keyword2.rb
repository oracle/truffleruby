def foo(n: 42, s: 42)
  [n, s]
end

foo(n: 42, s: "str")

__END__
# Classes
class Object
  private
  def foo: (?n: Integer, ?s: Integer | String) -> ([Integer, Integer | String])
end
