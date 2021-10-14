def foo
  if block_given?
    yield 42
  else
    1.0
  end
end

foo
foo {|n| n.to_s }

__END__
# Classes
class Object
  private
  def foo: ?{ (Integer) -> String } -> (Float | String)
end
