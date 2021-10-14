def foo(n:, s:)
  [n, s]
end

foo(n: 42, s: "str")

__END__
# Classes
class Object
  private
  def foo: (n: Integer, s: String) -> [Integer, String]
end
