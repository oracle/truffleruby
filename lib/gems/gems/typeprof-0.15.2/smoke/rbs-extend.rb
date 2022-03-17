def log
  C.foo
end

__END__
# Classes
class Object
  private
  def log: -> Integer
end
