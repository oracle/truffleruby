def foo
  yield "str"
end

def bar
  yield :sym
end

blk = -> x { x }
foo(&blk)
bar(&blk)

__END__
# Classes
class Object
  private
  def foo: { (:sym | String) -> (:sym | String) } -> (:sym | String)
  def bar: { (:sym | String) -> (:sym | String) } -> (:sym | String)
end
