class Foo
  include Enumerable
end

def log
  Foo.new.send(*:cycle)
end

__END__
# Classes
class Object
  private
  def log: -> Enumerator[untyped]
end

class Foo
  include Enumerable
end
