def foo
  cell = Cell.new(:a1, :b1, :c1)
  cell.set_a(:a2)
  cell.set_b(:b2)
  cell.set_c(:c2)
  cell
end

def bar
  foo.get_b
end

bar

__END__
# Classes
class Object
  private
  def foo: -> (Cell[:a1 | :a2, :b1 | :b2, :c1 | :c2])
  def bar: -> (:b1 | :b2)
end
