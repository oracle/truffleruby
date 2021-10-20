StringArray = ["str"] + ["str"]

def f1(&blk)
  yield *StringArray
  blk
end
def log1(a, o, r, c); end
f1 do |a, o=:opt, *r, c|
  log1(a, o, r, c)
end

def f2(&blk)
  yield :a, *StringArray
  blk
end
def log2(a, o, r, c); end
f2 do |a, o=:opt, *r, c|
  log2(a, o, r, c)
end

def f3(&blk)
  yield :a, :b, *StringArray
  blk
end
def log3(a, o, r, c); end
f3 do |a, o=:opt, *r, c|
  log3(a, o, r, c)
end

def f4(&blk)
  yield :a, :b, :c, *StringArray
  blk
end
def log4(a, o, r, c); end
f4 do |a, o=:opt, *r, c|
  log4(a, o, r, c)
end

def f5(&blk)
  yield :a, :b, :c, :d, *StringArray
  blk
end
def log5(a, o, r, c); end
f5 do |a, o=:opt, *r, c|
  log5(a, o, r, c)
end

def f6(&blk)
  yield :a, :b, :c, :d, :e, *StringArray
  blk
end
def log6(a, o, r, c); end
f6 do |a, o=:opt, *r, c|
  log6(a, o, r, c)
end

__END__
# Classes
class Object
  StringArray: Array[String]

  private
  def f1: { (*String) -> nil } -> (^(String, ?:opt | String, *String, String) -> nil)
  def log1: (String a, :opt | String o, Array[String] r, String c) -> nil
  def f2: { (:a, *String) -> nil } -> (^(:a, ?:opt | String, *String, String) -> nil)
  def log2: (:a a, :opt | String o, Array[String] r, String c) -> nil
  def f3: { (:a, :b, *String) -> nil } -> (^(:a, ?:b | :opt, *String, :b | String) -> nil)
  def log3: (:a a, :b | :opt o, Array[String] r, :b | String c) -> nil
  def f4: { (:a, :b, :c, *String) -> nil } -> (^(:a, ?:b | :opt, *:c | String, :c | String) -> nil)
  def log4: (:a a, :b | :opt o, Array[:c | String] r, :c | String c) -> nil
  def f5: { (:a, :b, :c, :d, *String) -> nil } -> (^(:a, ?:b | :opt, *:c | :d | String, :d | String) -> nil)
  def log5: (:a a, :b | :opt o, Array[:c | :d | String] r, :d | String c) -> nil
  def f6: { (:a, :b, :c, :d, :e, *String) -> nil } -> (^(:a, ?:b | :opt, *:c | :d | :e | String, :e | String) -> nil)
  def log6: (:a a, :b | :opt o, Array[:c | :d | :e | String] r, :e | String c) -> nil
end
