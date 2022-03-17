def foo
  cell = Cell.new("str")
  cell.set_elem(42) if rand < 0.5
  cell
end

def bar
  foo.get_elem
end

bar

__END__
# Classes
class Object
  private
  def foo: -> (Cell[Integer | String])
  def bar: -> (Integer | String)
end
