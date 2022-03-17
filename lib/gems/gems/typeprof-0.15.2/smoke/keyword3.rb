def foo(n: 42, s: [n])
  [n, s]
end

foo(n: 42, s: "str")

__END__
# Classes
class Object
  private
  def foo: (?n: Integer, ?s: String | [Integer]) -> ([Integer, String | [Integer]])
end
