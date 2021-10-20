class D
  def foo(x, y, z)
  end
end

class C
  def initialize(x)
    @target = x
  end

  def method_missing(m, *args)
    @target.send(m, *args)
  end
end

C.new(D.new).foo(:X, :Y, :Z)

__END__
# Classes
class D
  def foo: (:X x, :Y y, :Z z) -> nil
end

class C
  @target: D

  def initialize: (D x) -> D
  def method_missing: (:foo m, *:X | :Y | :Z args) -> nil
end
