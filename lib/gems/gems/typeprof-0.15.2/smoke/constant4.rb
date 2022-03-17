def foo
  Complex::I
end

foo

__END__
# Classes
class Object
  private
  def foo: -> Complex
end
