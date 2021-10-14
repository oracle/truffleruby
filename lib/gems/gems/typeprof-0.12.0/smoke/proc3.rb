def foo
  a = [1]
  -> do
    a[0] = "str"
  end.call
  a
end

foo
__END__
# Classes
class Object
  private
  def foo: -> [Integer]
end
