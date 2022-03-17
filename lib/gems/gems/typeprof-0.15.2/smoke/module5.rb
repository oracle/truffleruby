module Foo
  Module.new do
    include Foo
    extend Foo
  end
end

Foo.foo
__END__
# Errors
smoke/module5.rb:8: [error] undefined method: singleton(Foo)#foo

# Classes
module Foo
  extend Foo
  include Foo
end
