class User
  def foo(name: "str", age: 0)
    @name, @age = name, age
  end

  attr_reader :name, :age
end

__END__
# Classes
class User
  def foo: (?name: String | untyped, ?age: Integer | untyped) -> ([String | untyped, Integer | untyped])
  attr_reader name: String | untyped
  attr_reader age: Integer | untyped
end
