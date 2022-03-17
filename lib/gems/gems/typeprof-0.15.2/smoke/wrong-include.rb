module Bar
  def bar
    :bar
  end
end

module Foo
  mod = rand < 0.5 ? Bar : "Not module"

  include mod

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
  include Bar

  def foo: -> :foo
end
