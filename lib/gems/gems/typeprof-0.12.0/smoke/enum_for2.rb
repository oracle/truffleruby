class Foo
  def int_and_str_enum
    return enum_for(__method__) unless block_given?

    yield 1
    yield 2
    yield 3

    1.0
  end
end

__END__
# Classes
class Foo
  def int_and_str_enum: ?{ (Integer) -> untyped } -> (Enumerator[Integer, untyped] | Float)
end
