class C
  x = :x
  define_method(:foo) do
    x
  end
  define_method(:bar) do
    x
  end
end

C.new.foo

__END__
# Classes
class C
  def foo: -> :x
  def bar: -> :x
end
