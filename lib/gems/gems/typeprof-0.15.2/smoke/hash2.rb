def foo
  h = { int: 1, str: "str" }
  h[:int]
end

foo

__END__
# Classes
class Object
  private
  def foo: -> Integer
end
