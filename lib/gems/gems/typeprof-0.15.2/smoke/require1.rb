require_relative "require2"

class Foo
  def foo
  end
end

__END__
# Classes
class Foo
  def bar: -> nil
  def foo: -> nil
end
