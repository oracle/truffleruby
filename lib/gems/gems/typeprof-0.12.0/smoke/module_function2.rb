module Foo
  module_function
  def foo(x)
    x
  end
end

class Bar
  include Foo
  def bar
    foo(:y)
  end
end

Foo.foo(:x)
Bar.new.bar

__END__
# Classes
module Foo
  def foo: (:x | :y x) -> (:x | :y)
  def self.foo: (:x | :y x) -> (:x | :y)
end

class Bar
  include Foo

  def bar: -> (:x | :y)
end
