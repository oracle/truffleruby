module Bar
  def bar
    :bar
  end
end

module Foo
  mod = rand < 0.5 ? Bar : "Not module"

  extend mod

  def foo
    :foo
  end
end

__END__
# Classes
module Bar
  def bar: -> :bar
end

module Foo
  extend Bar

  def foo: -> :foo
end
