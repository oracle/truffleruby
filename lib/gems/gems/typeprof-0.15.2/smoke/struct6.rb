class Foo < Struct.new(:foo)
  def initialize(foo)
    super(foo.to_s)
  end
end

__END__
# Classes
class AnonymousStruct_generated_1 < Struct[untyped]
  attr_accessor foo(): untyped
end

class Foo < AnonymousStruct_generated_1
  def initialize: (untyped foo) -> void
end
