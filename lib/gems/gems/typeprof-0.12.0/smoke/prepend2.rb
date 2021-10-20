def log
  Foo.new.foo("str")
end

__END__
# Classes
class Object
  private
  def log: -> Integer
end
