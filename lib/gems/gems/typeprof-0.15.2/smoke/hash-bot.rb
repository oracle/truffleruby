def foo
  h = {}
  h[1] ||= []
  h
end

__END__
# Classes
class Object
  private
  def foo: -> Hash[Integer, Array[bot]]
end
