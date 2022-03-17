# two issues:
# (1) TypedProc should call `-> n { log1(n) }` with an Integer, but not implemented yet
# (2) log1 returns a String, which is inconsistent and should be reported

def log1(n)
  n.to_s # XXX: should be reported as "inconsistent with RBS"
end

def log2
  Foo.new.foo(-> n { log1(n) })
end

log2

__END__
# Classes
class Object
  private
  def log1: (untyped n) -> untyped
  def log2: -> String
end
