def foo(**kw)
  kw
end

foo(n: 42, s: "str")

__END__
# Classes
class Object
  private
  def foo: (**Integer | String) -> {n: Integer, s: String}
end
