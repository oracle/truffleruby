module Foo
  @extended = []
  def self.extended(klass)
    @extended << klass
  end
end

class C
  extend Foo
end

class D
  extend Foo
end

class E
  extend Foo
end

__END__
# Classes
module Foo
  self.@extended: Array[singleton(C) | singleton(D) | singleton(E)]

  def self.extended: (singleton(C) | singleton(D) | singleton(E) klass) -> (Array[singleton(C) | singleton(D) | singleton(E)])
end

class C
  extend Foo
end

class D
  extend Foo
end

class E
  extend Foo
end
