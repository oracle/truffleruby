module Foo
  @included = []
  def self.included(klass)
    @included << klass
  end
end

class C
  include Foo
end

class D
  include Foo
end

class E
  include Foo
end

__END__
# Classes
module Foo
  self.@included: Array[singleton(C) | singleton(D) | singleton(E)]

  def self.included: (singleton(C) | singleton(D) | singleton(E) klass) -> (Array[singleton(C) | singleton(D) | singleton(E)])
end

class C
  include Foo
end

class D
  include Foo
end

class E
  include Foo
end
