def foo
  1.0 * rand
end

foo
2.0 * unknown

__END__
# Errors
smoke/typed_method.rb:6: [error] undefined method: Object#unknown

# Classes
class Object
  private
  def foo: -> Float
end
