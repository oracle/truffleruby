def foo
  x = begin
    return x
    1
  end
end

__END__
# Classes
class Object
  private
  def foo: -> nil
end
