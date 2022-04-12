def foo
  yield
end

foo do
  raise
  "str"
rescue
  next 42
end

__END__
# Classes
class Object
  private
  def foo: { -> Integer } -> Integer
end
