def foo
  block_given?
end

foo {}

def bar
  block_given?
end

bar

def baz
  block_given?
end

baz {}
baz

def qux
  block_given?
end

qux(&unknown)

__END__
# Errors
smoke/block_given.rb:24: [error] undefined method: Object#unknown

# Classes
class Object
  private
  def foo: { -> nil } -> true
  def bar: -> false
  def baz: ?{ -> nil } -> bool
  def qux: -> bool
end
