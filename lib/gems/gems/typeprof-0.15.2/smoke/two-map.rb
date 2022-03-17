def foo
  [1, 2, 3].map {|n| n.to_s }
end

def bar
  [1, 2, 3].map {|n| n + 1 }
end

foo
bar

__END__
# Classes
class Object
  private
  def foo: -> Array[String]
  def bar: -> Array[Integer]
end
