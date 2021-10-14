class A
  def foo(x)
    bar(x)
  end

  def bar(x)
  end

  def self.test(x)
  end
end

class B < A
  def bar(x)
  end
end

A.new.foo(1)
B.new.foo("str")
B.new.bar(nil)
A.test(1)
B.test("str")

__END__
# Classes
class A
  def foo: (Integer | String x) -> nil
  def bar: (Integer | String x) -> nil
  def self.test: (Integer | String x) -> nil
end

class B < A
  def bar: ((Integer | String)? x) -> nil
end
