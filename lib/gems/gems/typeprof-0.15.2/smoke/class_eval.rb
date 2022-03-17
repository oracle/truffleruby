module Foo
end

module Bar
  Foo.class_eval do
    @foo = self
    def foo
      "str"
    end
  end
end

__END__
# Classes
module Foo
  self.@foo: singleton(Foo)

  def self.foo: -> String
end

module Bar
end
