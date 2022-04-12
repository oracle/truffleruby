def foo
  cell = Cell.new(42)
  cell.map {|s| (s + 1).to_s }
end

def bar
  cell = Cell.new(42)
  cell.map! {|s| (s + 1).to_s }
  cell
end

__END__
# Classes
class Object
  private
  def foo: -> Cell[String]
  def bar: -> (Cell[Integer | String])
end
