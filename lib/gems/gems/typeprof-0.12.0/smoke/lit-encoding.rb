def foo
  __ENCODING__
end

__END__
# Classes
class Object
  private
  def foo: -> Encoding
end
