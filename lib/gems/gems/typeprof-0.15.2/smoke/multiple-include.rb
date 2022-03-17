class Foo
  include Comparable
  include Comparable
  include Comparable
  def foo
  end
end

__END__
# Classes
class Foo
  include Comparable

  def foo: -> nil
end
