def foo
  x = nil
  [1, "str"].each do |y|
    x = y
  end
  x
end

foo

__END__
# Classes
class Object
  private
  def foo: -> ((Integer | String)?)
end
