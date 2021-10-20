class A
  %i[a b].each.with_index { |sym, i| define_method(sym) { i } }
end

def foo
  A.new.a + A.new.b
end

__END__
# Classes
class Object
  private
  def foo: -> Integer
end

class A
  def a: -> Integer
  def b: -> Integer
end
