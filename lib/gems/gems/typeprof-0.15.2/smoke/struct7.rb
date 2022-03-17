# I'm unsure how this is useful
# This test just checks if TypeProf raises no exception

class Foo < Struct
end

class Foo
  def initialize
    super
  end
end

__END__
# Classes
class Foo < Struct[untyped]
  def initialize: -> void
end
