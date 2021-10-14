def foo
  a = [[nil]]
  a[0] = a
  a
end

foo

__END__
# Classes
class Object
  private
  def foo: -> [untyped]
end
