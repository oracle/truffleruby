def f1(&blk)
  yield
  blk
end
def log1(a, o, c); end
f1 do |a, o=:opt, c|
  log1(a, o, c)
end

def f2(&blk)
  yield :a
  blk
end
def log2(a, o, c); end
f2 do |a, o=:opt, c|
  log2(a, o, c)
end

def f3(&blk)
  yield :a, :b
  blk
end
def log3(a, o, c); end
f3 do |a, o=:opt, c|
  log3(a, o, c)
end

def f4(&blk)
  yield :a, :b, :c
  blk
end
def log4(a, o, c); end
f4 do |a, o=:opt, c|
  log4(a, o, c)
end

def f5(&blk)
  yield :a, :b, :c, :d
  blk
end
def log5(a, o, c); end
f5 do |a, o=:opt, c|
  log5(a, o, c)
end

__END__
# Classes
class Object
  private
  def f1: { -> nil } -> ^(nil, ?:opt, nil) -> nil
  def log1: (nil a, :opt o, nil c) -> nil
  def f2: { (:a) -> nil } -> ^(:a, ?:opt, nil) -> nil
  def log2: (:a a, :opt o, nil c) -> nil
  def f3: { (:a, :b) -> nil } -> ^(:a, ?:opt, :b) -> nil
  def log3: (:a a, :opt o, :b c) -> nil
  def f4: { (:a, :b, :c) -> nil } -> (^(:a, ?:b | :opt, :c) -> nil)
  def log4: (:a a, :b | :opt o, :c c) -> nil
  def f5: { (:a, :b, :c, :d) -> nil } -> (^(:a, ?:b | :opt, :c) -> nil)
  def log5: (:a a, :b | :opt o, :c c) -> nil
end
