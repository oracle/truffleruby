def foo(x)
  if x
    42
  else
    "str"
  end
end

foo(true)
foo(false)

__END__
# Classes
class Object
  private
  def foo: (bool x) -> (Integer | String)
end
