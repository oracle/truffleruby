def log
  Foo.new.foo.call(42)
end

__END__
# Classes
class Object
  private
  def log: -> Integer
end
