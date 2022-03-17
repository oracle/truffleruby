def foo
  if rand < 0.5
    1
  end
end

def bar
  x = foo or raise("nil")
  x.times { }
end

__END__
# Classes
class Object
  private
  def foo: -> Integer?
  def bar: -> Integer
end
