def foo(x)
end

foo(UNDEFINED_CONSTANT)
foo(1)

C = "dummy"
class C
  def foo
  end
  self.new.foo
end

BAR = 1
BAR = "str"
def log(x)
end
log(BAR)

__END__
# Errors
smoke/constant2.rb:8: [error] the class "C" is String
smoke/constant2.rb:15: [warning] already initialized constant Object::BAR

# Classes
class Object
  BAR: String

  private
  def foo: (Integer | untyped x) -> nil
  def log: (String x) -> nil
end

class C
  def foo: -> nil
end
