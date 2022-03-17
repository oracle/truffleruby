def foo(arg)
  if Integer === arg
    arg
  else
    42
  end
end

foo("str")
foo(1)

__END__
# Classes
class Object
  private
  def foo: (Integer | String arg) -> Integer
end
