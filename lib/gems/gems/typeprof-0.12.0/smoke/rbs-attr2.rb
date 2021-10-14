class Foo
  def foo
    @name
  end
end

__END__
# Classes
class Foo
  def foo: -> String
end
