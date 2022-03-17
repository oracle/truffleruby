require_relative "require1"

class Foo
  def bar
  end
end

__END__
# Classes
class Foo
  def foo: -> nil
  def bar: -> nil
end
