def f1(&blk)
  yield
  blk
end
def log1(a, r, c); end
f1 do |a, *r, c|
  log1(a, r, c)
end

def f2(&blk)
  yield :a
  blk
end
def log2(a, r, c); end
f2 do |a, *r, c|
  log2(a, r, c)
end

def f3(&blk)
  yield :a, :b
  blk
end
def log3(a, r, c); end
f3 do |a, *r, c|
  log3(a, r, c)
end

def f4(&blk)
  yield :a, :b, :c
  blk
end
def log4(a, r, c); end
f4 do |a, *r, c|
  log4(a, r, c)
end

def f5(&blk)
  yield :a, :b, :c, :d
  blk
end
def log5(a, r, c); end
f5 do |a, *r, c|
  log5(a, r, c)
end

__END__
# Classes
class Object
  private
  def f1: { -> nil } -> ^(nil, *bot, nil) -> nil
  def log1: (nil a, Array[bot] r, nil c) -> nil
  def f2: { (:a) -> nil } -> ^(:a, *bot, nil) -> nil
  def log2: (:a a, Array[bot] r, nil c) -> nil
  def f3: { (:a, :b) -> nil } -> ^(:a, *bot, :b) -> nil
  def log3: (:a a, Array[bot] r, :b c) -> nil
  def f4: { (:a, :b, :c) -> nil } -> ^(:a, *:b, :c) -> nil
  def log4: (:a a, [:b] r, :c c) -> nil
  def f5: { (:a, :b, :c, :d) -> nil } -> (^(:a, *:b | :c, :d) -> nil)
  def log5: (:a a, [:b, :c] r, :d c) -> nil
end
