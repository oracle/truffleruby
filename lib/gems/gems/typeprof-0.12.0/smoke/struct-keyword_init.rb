Foo = Struct.new(:a, :b, :c, keyword_init: true)
Foo[a: 1, b: "str", c: 1.0]

__END__
# Classes
class Foo < Struct[untyped]
  attr_accessor a(): Integer
  attr_accessor b(): String
  attr_accessor c(): Float
end
