def log1(obj) end
def log2(obj) end
def log3(obj) end

obj = Bar.new("str")

log1(obj)
log2(obj.test_superclass)
log3(obj.test_module)

__END__
# Classes
class Object
  private
  def log1: (Bar[String] obj) -> nil
  def log2: (Array[String] obj) -> nil
  def log3: (Integer obj) -> nil
end
