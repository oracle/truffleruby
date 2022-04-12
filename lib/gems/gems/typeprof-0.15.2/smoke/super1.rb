def log(x)
end

class A; end
class B; end
class C; end
class X; end
class Y; end
class Z; end

class Foo
  def f(x)
    log(self)
    X.new
  end
end

class Bar < Foo
  def f(x)
    super(C.new)
    Y.new
  end
end

class Baz < Bar
  def f(x)
    super(B.new)
    Z.new
  end
end

Baz.new.f(A.new)

__END__
# Classes
class Object
  private
  def log: (Baz x) -> nil
end

class A
end

class B
end

class C
end

class X
end

class Y
end

class Z
end

class Foo
  def f: (C x) -> X
end

class Bar < Foo
  def f: (B x) -> Y
end

class Baz < Bar
  def f: (A x) -> Z
end
