def foo
  bar = Bar.new(1)
  bar.set(["str"]) # TODO: This should update Bar[Integer] to Bar[Integer | String]
  bar
end

__END__
# Classes
class Object
  private
  def foo: -> Bar[Integer]
end
