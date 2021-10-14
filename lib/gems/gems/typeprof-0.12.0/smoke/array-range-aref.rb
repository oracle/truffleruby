def f1(*ary)
  ary[1..3]
end

def f2(*ary)
  ary[1...3]
end

def f3(*ary)
  ary[1..-3]
end

def f4(*ary)
  ary[1...-3]
end

def f5(*ary)
  ary[-4..3]
end

def f6(*ary)
  ary[-4...3]
end

def f7(*ary)
  ary[-4..-3]
end

def f8(*ary)
  ary[-4...-3]
end

def f9(*ary)
  ary[...3]
end

def f10(*ary)
  ary[3..]
end

def dispatch(*ary)
  f1(*ary)
  f2(*ary)
  f3(*ary)
  f4(*ary)
  f5(*ary)
  f6(*ary)
  f7(*ary)
  f8(*ary)
  f9(*ary)
  f10(*ary)
end

dispatch(:a, :b, :c, :d, :e)

__END__
# Classes
class Object
  private
  def f1: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f2: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f3: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f4: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f5: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f6: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f7: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f8: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f9: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def f10: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
  def dispatch: (*:a | :b | :c | :d | :e ary) -> (Array[:a | :b | :c | :d | :e])
end
