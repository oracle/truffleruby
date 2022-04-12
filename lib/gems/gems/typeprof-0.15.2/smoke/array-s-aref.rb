def foo
  Array[42, "str"]
end

foo

__END__
# Classes
class Object
  private
  def foo: -> (Array[Integer | String])
end
