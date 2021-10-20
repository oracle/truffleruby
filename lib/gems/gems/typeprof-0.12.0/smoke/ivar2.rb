class Foo
  def initialize
    @array = []
    @hash = {}
  end

  def set
    @array << 1
    @array << "str"
    @array << :sym
    @hash[:a] = 1
    @hash[:b] = "str"
    @hash[:c] = :sym
  end

  attr_reader :array, :hash
end

Foo.new.array
Foo.new.hash
Foo.new.set

__END__
# Classes
class Foo
  def initialize: -> Hash[bot, bot]
  def set: -> :sym
  attr_reader array: Array[:sym | Integer | String]
  attr_reader hash: {a: Integer, b: String, c: :sym}
end
