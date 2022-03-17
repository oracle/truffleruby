# This code is created by simplifying matrix.rb.
# The issue occurs when `Array.new` creates a cell-type container,
# so this test will be obsolete, but keep it just for case of regression.
#
# https://github.com/ruby/typeprof/issues/14

class Foo
  def initialize(ivar)
    @ivar = ivar
  end

  def foo(n)
    @ivar.each_with_index { }
    nil
  end
end

Foo.new([])

rows = Array.new(1) do |i|
  Array.new(1) do |j|
    "str"
  end
end
obj = Foo.new(rows)

obj.foo(:a)
obj.foo(:b)

__END__
# Classes
class Foo
  @ivar: Array[Array[String]]

  def initialize: (Array[Array[String]] ivar) -> void
  def foo: (:a | :b n) -> nil
end
