class Foo
  def foo(**r)
    @r = r[42]
  end
end

__END__
# Classes
class Foo
  @r: Integer

# def foo: (**Hash[Integer, Integer]) -> void
end
