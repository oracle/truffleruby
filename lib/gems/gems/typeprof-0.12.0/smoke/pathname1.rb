require "pathname"

def foo
  Pathname.new("foo")
end

foo

__END__
# Classes
class Object
  private
  def foo: -> Pathname
end
