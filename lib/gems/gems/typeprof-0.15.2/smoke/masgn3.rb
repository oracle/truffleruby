def foo
  a, b, c = 42
  [a, b, c]
end

foo

__END__
# Classes
class Object
  private
  def foo: -> [Integer, nil, nil]
end
