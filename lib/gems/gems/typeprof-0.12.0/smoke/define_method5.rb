class Foo
  foo = 42
  f = proc { foo }

  define_method(:foo, &f)
end

__END__
# Classes
class Foo
  def foo: -> Integer
end
