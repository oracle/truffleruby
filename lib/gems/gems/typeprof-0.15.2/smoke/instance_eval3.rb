:foo.instance_eval do
  @foo = 1
end

[].instance_eval do
  @bar = 1
end

{}.instance_eval do
  @baz = 1
end

__END__
# Classes
class Array
  @bar: Integer
end

class Hash
  @baz: Integer
end

class Symbol
  @foo: Integer
end
