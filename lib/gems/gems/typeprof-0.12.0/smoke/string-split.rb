def foo
  "".split("").map {|n| n.to_i }
end

foo

__END__
# Classes
class Object
  private
  def foo: -> Array[Integer]
end
