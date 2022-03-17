def foo
  f = -> x { x }
  f[42]
end

def bar
  f = -> x { "str" }
  f[42]
end

foo
bar

__END__
# Classes
class Object
  private
  def foo: -> Integer
  def bar: -> String
end
