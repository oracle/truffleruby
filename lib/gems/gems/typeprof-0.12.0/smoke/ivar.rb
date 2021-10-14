class Foo
  def foo=(x)
    @foo = x
  end

  def foo
    @foo
  end
end

def log(x)
end

Foo.new.foo = 1
log(Foo.new.foo)
Foo.new.foo = "str"
log(Foo.new.foo)

__END__
# Classes
class Object
  private
  def log: (Integer | String x) -> nil
end

class Foo
  @foo: Integer | String

  def foo=: (Integer | String x) -> (Integer | String)
  def foo: -> (Integer | String)
end
