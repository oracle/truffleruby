class Foo
  def foo(n)
    n
  end

  def self.bar
    new.foo(1)
  end
end

__END__
# Classes
class Foo
  def foo: (Integer | untyped n) -> (Integer | untyped)
  def self.bar: -> (Integer | untyped)
end
