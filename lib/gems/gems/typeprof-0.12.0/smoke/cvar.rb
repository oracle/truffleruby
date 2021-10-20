def log1(x)
end

def log2(x)
end

class A
  FOO=1
  @@var = 1
  log1(@@var)
  def foo
    log2(@@var)
  end
end

A.new.foo

__END__
# Classes
class Object
  private
  def log1: (Integer x) -> nil
  def log2: (Integer x) -> nil
end

class A
  FOO: Integer
  @@var: Integer

  def foo: -> nil
end
