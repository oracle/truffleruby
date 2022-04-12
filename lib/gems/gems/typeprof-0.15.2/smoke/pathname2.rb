require "pathname"

def foo
  Pathname("foo")
end

foo

__END__
# Classes
class Object
  private
  def foo: -> Pathname
end
