class Foo
  def initialize(a)
    @a = a
  end
  attr_reader :a

  attr_writer :b
  def get_b
    @b
  end

  attr_accessor :c
end

foo = Foo.new(:aaa)
foo.b = :bbb
foo.get_b
foo.c = :ccc

__END__
# Classes
class Foo
  def initialize: (:aaa a) -> void
  attr_reader a: :aaa
  attr_writer b: :bbb
  def get_b: -> :bbb
  attr_accessor c: :ccc
end
