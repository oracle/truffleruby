def foo
  h = {}
  h[:int] = 1
  h[:str] = "str"
  h
end

foo
__END__
# Classes
class Object
  private
  def foo: -> {int: Integer, str: String}
end
