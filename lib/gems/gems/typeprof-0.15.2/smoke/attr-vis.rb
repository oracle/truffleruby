class Foo
  def initialize
    @foo = 42
  end

  private
  attr_accessor :foo

  public
  def get_foo
    foo
  end
  def set_foo(arg)
    self.foo = arg
  end
  def get_bar
    bar
  end
  def set_bar(arg)
    self.bar = arg
  end
end

Foo.new.set_foo("str")
Foo.new.set_bar("str")

__END__
# Errors
smoke/attr-vis.rb:20: [warning] inconsistent assignment to RBS-declared variable

# Classes
class Foo
  def initialize: -> void

  private
  attr_accessor foo: Integer | String

  public
  def get_foo: -> (Integer | String)
  def set_foo: (String arg) -> String
  def get_bar: -> Integer
  def set_bar: (String arg) -> String
end
