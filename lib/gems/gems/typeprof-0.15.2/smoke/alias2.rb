class Foo
  class << self
    alias [] new
  end

  def initialize(a, b, c)
    @a, @b, @c = a, b, c
  end
end

Foo[:x, :y, :z]
__END__
# Classes
class Foo
  @a: :x
  @b: :y
  @c: :z

  alias self.[] self.new
  def initialize: (:x a, :y b, :z c) -> void
end
