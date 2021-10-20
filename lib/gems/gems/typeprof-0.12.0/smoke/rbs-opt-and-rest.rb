def foo
  Foo.foo
end

__END__
# Classes
class Object
  private
  def foo: -> void
end
