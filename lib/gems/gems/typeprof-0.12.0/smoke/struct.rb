# tentative: Currently, the hidden instance variable belongs to Foo, not Anonymous Struct.
# Thus, it outputs "untyped"
class Foo < Struct.new(:a)
end
Foo.new.a = 1
__END__
# Classes
class AnonymousStruct_generated_1 < Struct[untyped]
  attr_accessor a(): untyped
end

class Foo < AnonymousStruct_generated_1
end
