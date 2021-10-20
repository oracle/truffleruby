class Foo
  def bar(n)
    :BAR
  end

  define_method(:foo) do |n|
    bar(:FOO)
  end
end

__END__
# Classes
class Foo
  def bar: (:FOO | untyped n) -> :BAR
  def foo: (untyped n) -> :BAR
end
