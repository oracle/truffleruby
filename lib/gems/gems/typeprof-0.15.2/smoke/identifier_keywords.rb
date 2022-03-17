def type(type)
end

def out(*out)
end

def untyped(untyped:)
end

__END__
# Classes
class Object
  private
  def type: (untyped `type`) -> nil
  def out: (*untyped `out`) -> nil
  def untyped: (untyped: untyped) -> nil
end
