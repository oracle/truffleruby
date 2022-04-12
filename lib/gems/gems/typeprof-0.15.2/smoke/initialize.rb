class A
  def initialize(x)
    @int = 1
    @str = "str"
    @val = x
  end
end

def log(x)
end
log A.new(1)
A.new("str")
A.new(nil)

__END__
# Classes
class Object
  private
  def log: (A x) -> nil
end

class A
  @int: Integer
  @str: String
  @val: (Integer | String)?

  def initialize: ((Integer | String)? x) -> void
end
