def foo
  a = [[42]]
  a[0][0] = "str"
  a
end

def bar(a)
  a[0][0] = "str"
  a
end

def log(a)
end

foo
a = [[42]]
bar(a)
# limitation: a is kept as [[Integer]]
log(a)

__END__
# Classes
class Object
  private
  def foo: -> [[String]]
  def bar: ([[Integer]] a) -> [[String]]
  def log: ([[Integer]] a) -> nil
end
