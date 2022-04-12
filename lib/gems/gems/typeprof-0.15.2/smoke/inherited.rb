class Foo
  @inherited = []
  def self.inherited(klass)
    @inherited << klass
  end
end

class Bar < Foo
end

class Baz < Foo
end

__END__
# Classes
class Foo
  self.@inherited: Array[singleton(Bar) | singleton(Baz)]

  def self.inherited: (singleton(Bar) | singleton(Baz) klass) -> (Array[singleton(Bar) | singleton(Baz)])
end

class Bar < Foo
end

class Baz < Foo
end
