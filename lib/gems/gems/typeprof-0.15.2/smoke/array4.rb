def foo
  while 1+1
    a = [1]
  end
  a
end

foo

__END__
# Classes
class Object
  private
  def foo: -> [Integer]?
end
