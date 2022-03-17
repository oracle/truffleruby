def foo(obj)
  if obj ? true : obj.foo(1)
    "foo"
  end
end

__END__
# Classes
class Object
  private
  def foo: (untyped obj) -> String?
end
