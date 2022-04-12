# a test for a recursive proc that receives (or returns) itself
def foo
  f = -> x { x }
  f[f]
  f
end

__END__
# Classes
class Object
  private
  def foo: -> ^(untyped) -> untyped
end
