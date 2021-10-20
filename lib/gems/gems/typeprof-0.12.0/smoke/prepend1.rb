module Mod
  def foo
    [42, super(:sym)]
  end
end

class Foo
  prepend Mod

  def foo(sym_arg)
    sym_arg.to_s
  end

  def bar
    foo
  end
end

__END__
# Errors
smoke/prepend1.rb:3: [error] no superclass method: Mod#foo

# Classes
module Mod
  def foo: -> ([Integer, String | untyped])
end

class Foo
  prepend Mod

  def foo: (:sym | untyped sym_arg) -> (String | untyped)
  def bar: -> ([Integer, String | untyped])
end
