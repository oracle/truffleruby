def foo(x)
  42
end

def bar
  begin
    x = 1
    x = "str"
    x = :sym
  rescue
  end
  foo(x)
end

bar

__END__
# Classes
class Object
  private
  def foo: ((:sym | Integer | String)? x) -> Integer
  def bar: -> Integer
end
