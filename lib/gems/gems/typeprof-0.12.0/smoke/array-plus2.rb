class Foo
  def foo
    @var = [1]
    @var = @var + [2]
  end
end

Foo.new.foo

__END__
# Classes
class Foo
  @var: Array[Integer]

  def foo: -> Array[Integer]
end
