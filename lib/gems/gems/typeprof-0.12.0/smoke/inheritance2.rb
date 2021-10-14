class SuperBase
  def self.foo
    self
  end
end

class Base < SuperBase
  def self.foo
    super
  end
end

class A < Base
  foo
end

class B < Base
  foo
end

__END__
# Classes
class SuperBase
  def self.foo: -> (singleton(A) | singleton(B))
end

class Base < SuperBase
  def self.foo: -> (singleton(A) | singleton(B))
end

class A < Base
end

class B < Base
end
