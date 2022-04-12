def foo
  5.instance_eval { i }
end

foo

__END__
# Classes
class Object
  private
  def foo: -> Complex
end
