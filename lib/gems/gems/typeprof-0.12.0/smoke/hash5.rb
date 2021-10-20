class Foo
  def foo
    h = {}
    key = ["str"]
    h[key] = 1
    h[key]
  end
end
Foo.new.foo
__END__
# Classes
class Foo
  def foo: -> Integer
end
