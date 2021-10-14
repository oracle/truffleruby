def f(a, b, c)
  ary = [nil]
  foo, bar, ary[0] = a, b, c
  ary[0]
end

f(:a, :b, :c)

__END__
# Classes
class Object
  private
  def f: (:a a, :b b, :c c) -> :c
end
