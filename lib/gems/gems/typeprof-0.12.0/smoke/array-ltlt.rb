def foo
  a = []
  a << 1
  a << "str"
  a
end

foo
__END__
# Classes
class Object
  private
  def foo: -> (Array[Integer | String])
end
