def foo(req:)
  req
end

foo()
foo(req: 1)

__END__
# Errors
smoke/keyword5.rb:5: [error] no argument for required keywords

# Classes
class Object
  private
  def foo: (req: Integer) -> Integer
end
