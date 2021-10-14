class Foo
  def foo=(x)
    @foo = x
  end

  def foo
    @foo
  end
end

Foo.new.foo = 1
Foo.new.foo = "str"
Foo.new.foo

__END__
# Classes
class Foo
  @foo: Integer | String

  def foo=: (Integer | String x) -> (Integer | String)
  def foo: -> (Integer | String)
end
