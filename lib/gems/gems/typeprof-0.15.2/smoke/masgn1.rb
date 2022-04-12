def foo
  ary = [1, "str", :sym, nil]
  a, *rest, z = *ary
  [a, rest, z]
end

foo

__END__
# Classes
class Object
  private
  def foo: -> [Integer, [String, :sym], nil]
end
