def f1
  yield :a, :b, :c
end
def log1(x); end
f1 {|x| log1(x) }

def f2
  yield :a, :b, :c
end
def log2(x); end
f2 {|x,| log2(x) }

def f3
  yield [:a, :b, :c]
end
def log3(x); end
f3 {|x| log3(x) }

def f4
  yield [:a, :b, :c]
end
def log4(x); end
f4 {|x,| log4(x) }

__END__
# Classes
class Object
  private
  def f1: { (:a, :b, :c) -> nil } -> nil
  def log1: (:a x) -> nil
  def f2: { (:a, :b, :c) -> nil } -> nil
  def log2: (:a x) -> nil
  def f3: { ([:a, :b, :c]) -> nil } -> nil
  def log3: ([:a, :b, :c] x) -> nil
  def f4: { ([:a, :b, :c]) -> nil } -> nil
  def log4: (:a x) -> nil
end
