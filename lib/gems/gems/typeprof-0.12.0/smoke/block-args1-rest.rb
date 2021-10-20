StringArray = ["str"] + ["str"]

def f1(&blk)
  yield *StringArray
  blk
end
def log1(a, o, c); end
f1 do |a, o=:opt, c|
  log1(a, o, c)
end

def f2(&blk)
  yield :a, *StringArray
  blk
end
def log2(a, o, c); end
f2 do |a, o=:opt, c|
  log2(a, o, c)
end

def f3(&blk)
  yield :a, :b, *StringArray
  blk
end
def log3(a, o, c); end
f3 do |a, o=:opt, c|
  log3(a, o, c)
end

def f4(&blk)
  yield :a, :b, :c, *StringArray
  blk
end
def log4(a, o, c); end
f4 do |a, o=:opt, c|
  log4(a, o, c)
end

def f5(&blk)
  yield :a, :b, :c, :d, *StringArray
  blk
end
def log5(a, o, c); end
f5 do |a, o=:opt, c|
  log5(a, o, c)
end

__END__
# Classes
class Object
  StringArray: Array[String]

  private
  def f1: { (*String) -> nil } -> (^(String, ?:opt | String, String) -> nil)
  def log1: (String a, :opt | String o, String c) -> nil
  def f2: { (:a, *String) -> nil } -> (^(:a, ?:opt | String, String) -> nil)
  def log2: (:a a, :opt | String o, String c) -> nil
  def f3: { (:a, :b, *String) -> nil } -> (^(:a, ?:b | :opt, :b | String) -> nil)
  def log3: (:a a, :b | :opt o, :b | String c) -> nil
  def f4: { (:a, :b, :c, *String) -> nil } -> (^(:a, ?:b | :opt, :b | :c) -> nil)
  def log4: (:a a, :b | :opt o, :b | :c c) -> nil
  def f5: { (:a, :b, :c, :d, *String) -> nil } -> (^(:a, ?:b | :opt, :b | :c) -> nil)
  def log5: (:a a, :b | :opt o, :b | :c c) -> nil
end
