F = -> x { "str" }

def foo(&blk)
  blk.call(:sym, &F)
end

foo do |dummy, &blk|
  blk.call(42)
end

# truly expected:
# Object#foo :: (&Proc[(Symbol, &Proc[(Integer) -> String]) -> String]) -> String

__END__
# Classes
class Object
  F: ^(Integer) -> String

  private
  def foo: { (:sym) -> String } -> String
end
