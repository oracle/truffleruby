def f1
end
f1

def f2(x, y, z)
end
f2(:x, :y, :z)

def f3(x = "str", y = "str")
end
f3

def f4(*r)
end
f4(:a, :b, :c)

def f5(x, y = "str", z)
end
f5(:x, :z)

def f6(k:)
end
f6(k: :kk)

def f7(k: 42)
end
f7

def f8(k: "str")
end
f8

def f9(**kw)
end
f9(k: :kk)

def f10(&blk)
  blk.call(1)
end
f10 {|n| }

__END__
# Classes
class Object
  private
  def f1: -> nil
  def f2: (:x x, :y y, :z z) -> nil
  def f3: (?String x, ?String y) -> nil
  def f4: (*:a | :b | :c r) -> nil
  def f5: (:x x, ?String y, :z z) -> nil
  def f6: (k: :kk) -> nil
  def f7: (?k: Integer) -> nil
  def f8: (?k: String) -> nil
  def f9: (**:kk) -> nil
  def f10: { (Integer) -> nil } -> nil
end
