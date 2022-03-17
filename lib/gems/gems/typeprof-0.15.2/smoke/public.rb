class Foo
  def foo
  end

  private

  def bar
  end

  public

  def baz
  end

  private def qux
  end

  def corge
  end
end

__END__
# Classes
class Foo
  def foo: -> nil

  private
  def bar: -> nil

  public
  def baz: -> nil

  private
  def qux: -> nil

  public
  def corge: -> nil
end
