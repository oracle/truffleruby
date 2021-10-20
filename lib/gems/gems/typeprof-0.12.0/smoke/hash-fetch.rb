h = { a: :A, b: :B }

def foo(h)
  h.fetch(:a)     #=> :A | :B
end
def bar(h)
  h.fetch(:a, :C) #=> :A | :B | :C
end
def baz(h)
  n = nil
  [h.fetch(:a) do |k| #=> :A | :B | :C
    n = k #=> :A | :B
    :C
  end, n]
end

foo(h)
bar(h)
baz(h)

__END__
# Classes
class Object
  private
  def foo: ({a: :A, b: :B} h) -> (:A | :B)
  def bar: ({a: :A, b: :B} h) -> (:A | :B | :C)
  def baz: ({a: :A, b: :B} h) -> ([:A | :B | :C, (:a | :b)?])
end
