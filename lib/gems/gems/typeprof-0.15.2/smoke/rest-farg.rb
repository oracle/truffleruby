def foo
  a = ["", ""]
  "".start_with?("", *a)
end

__END__
# Classes
class Object
  private
  def foo: -> bool
end
