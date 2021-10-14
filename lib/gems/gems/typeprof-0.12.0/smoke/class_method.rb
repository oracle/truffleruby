class Test
  def self.foo(x)
    x
  end

  def Test.bar(x)
    x
  end

  #class << self
  #  alias baz bar
  #end
end

Test.foo(1)
Test.bar("str")
#Test.baz(:sym)
# Test#foo: (Integer) -> Integer

__END__
# Classes
class Test
  def self.foo: (Integer x) -> Integer
  def self.bar: (String x) -> String
end
