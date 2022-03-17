def foo
  h = { a: 42 }
  h0 = h.merge!({ b: "str" })
  return h0, h
end

__END__
# Classes
class Object
  private
  def foo: -> [{a: Integer}, {a: Integer, b: String}]
end
