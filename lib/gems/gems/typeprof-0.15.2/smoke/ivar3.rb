class Foo
  def initialize
    @foo = "str"
    @bar = "str"
  end
end

__END__
# Errors
smoke/ivar3.rb:3: [warning] inconsistent assignment to RBS-declared variable

# Classes
class Foo
  @bar: String

  def initialize: -> void
end
