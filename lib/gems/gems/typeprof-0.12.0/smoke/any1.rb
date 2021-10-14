def foo
  x = 1.undefined_method
  x.undefined_method2
end

foo

__END__
# Errors
smoke/any1.rb:2: [error] undefined method: Integer#undefined_method

# Classes
class Object
  private
  def foo: -> untyped
end
