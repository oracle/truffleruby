module Foo
  class << self
    def foo
    end

    def foo2
      foo
    end
  end

  def bar
  end

  def bar2
    bar
  end
end

__END__
# Classes
module Foo
# def self.foo: -> Integer
# def bar: -> Integer
  def self.foo2: -> Integer
  def bar2: -> Integer
end
