def foo
  __method__
end

def log(dummy)
end

def bar
  log(__method__)
end

__END__
# Classes
class Object
  private
  def foo: -> :foo
  def log: (:bar | untyped dummy) -> nil
  def bar: -> nil
end
