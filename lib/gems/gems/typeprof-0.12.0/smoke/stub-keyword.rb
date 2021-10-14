def foo(age: nil)
end

foo(age: 1)
__END__
# Classes
class Object
  private
  def foo: (?age: Integer?) -> nil
end
