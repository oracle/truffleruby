class C0; end
class C1 < C0; def foo(n); end; end
class C2 < C0; def foo(n); end; end
class C3 < C0; def foo(n); end; end
class C4 < C0; def foo(n); end; end
class C5 < C0; def foo(n); end; end
class C6 < C0; def foo(n); end; end
class C7 < C0; def foo(n); end; end
class C8 < C0; def foo(n); end; end
class C9 < C0; def foo(n); end; end
class C10 < C0; def foo(n); end; end
class C11 < C0; def foo(n); end; end
class C12 < C0; def foo(n); end; end

def dispatch_foo(n)
  n.foo("str")
end

dispatch_foo(C1.new)
dispatch_foo(C2.new)
dispatch_foo(C3.new)
dispatch_foo(C4.new)
dispatch_foo(C5.new)
dispatch_foo(C6.new)
dispatch_foo(C7.new)
dispatch_foo(C8.new)
dispatch_foo(C9.new)
dispatch_foo(C10.new)

__END__
# Classes
class Object
  private
  def dispatch_foo: (C0 n) -> nil
end

class C0
end

class C1 < C0
  def foo: (String n) -> nil
end

class C2 < C0
  def foo: (String n) -> nil
end

class C3 < C0
  def foo: (String n) -> nil
end

class C4 < C0
  def foo: (String n) -> nil
end

class C5 < C0
  def foo: (String n) -> nil
end

class C6 < C0
  def foo: (String n) -> nil
end

class C7 < C0
  def foo: (String n) -> nil
end

class C8 < C0
  def foo: (String n) -> nil
end

class C9 < C0
  def foo: (String n) -> nil
end

class C10 < C0
  def foo: (String n) -> nil
end

class C11 < C0
  def foo: (String n) -> nil
end

class C12 < C0
  def foo: (String n) -> nil
end
