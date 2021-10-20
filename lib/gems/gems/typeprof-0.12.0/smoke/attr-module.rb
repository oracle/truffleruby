module Foo
  attr_accessor :foo
  module_function :foo=, :foo
end

class Bar
  include Foo
end

Foo.foo = 42
Bar.new.foo = "str"

# XXX: the output may be improved

__END__
# Classes
module Foo
  self.@foo: Integer

  attr_accessor foo: untyped
  attr_accessor self.foo: Integer
end

class Bar
  include Foo
  @foo: String
end
