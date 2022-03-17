def foo(n = 1)
end

foo("str")

__END__
# Classes
class Object
  private
  def foo: (?Integer | String n) -> nil
end
