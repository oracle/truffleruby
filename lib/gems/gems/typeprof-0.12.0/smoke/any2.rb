def foo
  yield
end
def bar
  yield
  1
end

foo
bar

__END__
# Classes
class Object
  private
  def foo: -> untyped
  def bar: -> Integer
end
