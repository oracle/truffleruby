def foo
  ary = ["bar", "baz"]
  case "foo"
  when "foo"
    1
  when *ary
    :sym
  end
end

foo

__END__
# Classes
class Object
  private
  def foo: -> ((:sym | Integer)?)
end
