def foo
  cell = Cell.new
  cell.set { Foo.new(:a, :b) }
  cell.dummy { }
  cell
end

__END__
# Classes
class Object
  private
  def foo: -> Cell[Foo[:a, :b]]
end
