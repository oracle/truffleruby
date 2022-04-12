class Foo
  def f
    super
  end
end

Foo.new.f

__END__
# Errors
smoke/super2.rb:3: [error] no superclass method: Foo#f

# Classes
class Foo
  def f: -> untyped
end
