def test_foo
  h = C.new.foo
  return h[:aaa], h[:bbb]
end

def test_bar
  h = { }
  h[:a] = 42
  C.new.bar(h)
end

__END__
# Classes
class Object
  private
  def test_foo: -> [Integer, String]
  def test_bar: -> Integer
end
