def foo(f)
  f.call(1)
end

foo(-> x { "str" })

__END__
# Classes
class Object
  private
  def foo: (^(Integer) -> String f) -> String
end
