def log
  Foo.new.bar
end

__END__
# Classes
class Object
  private
  def log: -> Integer
end
