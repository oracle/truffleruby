def foo
  nil.send
end

__END__
# Classes
class Object
  private
  def foo: -> untyped
end
