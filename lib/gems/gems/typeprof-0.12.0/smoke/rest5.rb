def foo(*args)
  args
end

foo(42)
__END__
# Classes
class Object
  private
  def foo: (*Integer args) -> [Integer]
end
