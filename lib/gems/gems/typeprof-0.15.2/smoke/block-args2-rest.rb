StringArray = ["str"] + ["str"]

def f1(&blk)
  yield *StringArray
  blk
end
def log1(a, r, c); end
f1 do |a, *r, c|
  log1(a, r, c)
end

def f2(&blk)
  yield :a, *StringArray
  blk
end
def log2(a, r, c); end
f2 do |a, *r, c|
  log2(a, r, c)
end

def f3(&blk)
  yield :a, :b, *StringArray
  blk
end
def log3(a, r, c); end
f3 do |a, *r, c|
  log3(a, r, c)
end

def f4(&blk)
  yield :a, :b, :c, *StringArray
  blk
end
def log4(a, r, c); end
f4 do |a, *r, c|
  log4(a, r, c)
end

def f5(&blk)
  yield :a, :b, :c, :d, *StringArray
  blk
end
def log5(a, r, c); end
f5 do |a, *r, c|
  log5(a, r, c)
end

__END__
# Classes
class Object
  StringArray: Array[String]

  private
  def f1: { (*String) -> nil } -> ^(String, *String, String) -> nil
  def log1: (String a, Array[String] r, String c) -> nil
  def f2: { (:a, *String) -> nil } -> ^(:a, *String, String) -> nil
  def log2: (:a a, Array[String] r, String c) -> nil
  def f3: { (:a, :b, *String) -> nil } -> (^(:a, *:b | String, :b | String) -> nil)
  def log3: (:a a, Array[:b | String] r, :b | String c) -> nil
  def f4: { (:a, :b, :c, *String) -> nil } -> (^(:a, *:b | :c | String, :c | String) -> nil)
  def log4: (:a a, Array[:b | :c | String] r, :c | String c) -> nil
  def f5: { (:a, :b, :c, :d, *String) -> nil } -> (^(:a, *:b | :c | :d | String, :d | String) -> nil)
  def log5: (:a a, Array[:b | :c | :d | String] r, :d | String c) -> nil
end
