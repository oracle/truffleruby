class Foo
  define_method(:foo) do |*messages, **kw|
    bar(*messages, **kw)
  end

  def bar(*messages, **kw)
  end
end

__END__
# Classes
class Foo
# def foo: () -> NilClass
  def bar: (*untyped messages, **untyped) -> nil
end
