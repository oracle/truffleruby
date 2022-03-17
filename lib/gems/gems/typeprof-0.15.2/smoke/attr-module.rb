module Foo
  attr_accessor :foo
  module_function :foo=, :foo
end

class Bar
  include Foo
end

Foo.foo = 42
Bar.new.foo = "str"

__END__
# Classes
module Foo
  self.@foo: Integer

  attr_accessor foo: String
  attr_accessor self.foo: Integer
end

class Bar
  include Foo
end
