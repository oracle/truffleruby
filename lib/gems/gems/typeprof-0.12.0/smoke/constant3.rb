def foo
  1::Foo
end
foo
__END__
# Classes
class Object
  private
  def foo: -> untyped
end
