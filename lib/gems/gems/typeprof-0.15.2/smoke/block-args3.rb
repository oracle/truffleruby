def f1(&blk)
  yield
  blk
end
def log1(a, o, r, c); end
f1 do |a, o=:opt, *r, c|
  log1(a, o, r, c)
end

def f2(&blk)
  yield :a
  blk
end
def log2(a, o, r, c); end
f2 do |a, o=:opt, *r, c|
  log2(a, o, r, c)
end

def f3(&blk)
  yield :a, :b
  blk
end
def log3(a, o, r, c); end
f3 do |a, o=:opt, *r, c|
  log3(a, o, r, c)
end

def f4(&blk)
  yield :a, :b, :c
  blk
end
def log4(a, o, r, c); end
f4 do |a, o=:opt, *r, c|
  log4(a, o, r, c)
end

def f5(&blk)
  yield :a, :b, :c, :d
  blk
end
def log5(a, o, r, c); end
f5 do |a, o=:opt, *r, c|
  log5(a, o, r, c)
end

def f6(&blk)
  yield :a, :b, :c, :d, :e
  blk
end
def log6(a, o, r, c); end
f6 do |a, o=:opt, *r, c|
  log6(a, o, r, c)
end

__END__
# Classes
class Object
  private
  def f1: { -> nil } -> ^(nil, ?:opt, *bot, nil) -> nil
  def log1: (nil a, :opt o, Array[bot] r, nil c) -> nil
  def f2: { (:a) -> nil } -> ^(:a, ?:opt, *bot, nil) -> nil
  def log2: (:a a, :opt o, Array[bot] r, nil c) -> nil
  def f3: { (:a, :b) -> nil } -> ^(:a, ?:opt, *bot, :b) -> nil
  def log3: (:a a, :opt o, Array[bot] r, :b c) -> nil
  def f4: { (:a, :b, :c) -> nil } -> (^(:a, ?:b | :opt, *bot, :c) -> nil)
  def log4: (:a a, :b | :opt o, Array[bot] r, :c c) -> nil
  def f5: { (:a, :b, :c, :d) -> nil } -> (^(:a, ?:b | :opt, *:c, :d) -> nil)
  def log5: (:a a, :b | :opt o, [:c] r, :d c) -> nil
  def f6: { (:a, :b, :c, :d, :e) -> nil } -> (^(:a, ?:b | :opt, *:c | :d, :e) -> nil)
  def log6: (:a a, :b | :opt o, [:c, :d] r, :e c) -> nil
end
