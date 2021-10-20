class Foo
  @foo = 1
end

__END__
# Classes
class Foo
  self.@foo: Integer
end
