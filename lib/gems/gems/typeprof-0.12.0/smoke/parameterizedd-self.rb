class Object
  def foo
    self
  end
end

def bar(ary)
  ary.foo
end

bar([])

__END__
# Classes
class Object
  def foo: -> Array[bot]

  private
  def bar: (Array[bot] ary) -> Array[bot]
end
