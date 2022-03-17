# RUBY_VERSION >= 3.0
# NO_SHOW_ERRORS

def foo
  case [:a, :b, :c]
  in [a, b, :c]
    # Due to very subtle detail of bytecode, the variables "a" and "b" could be nil
    return a, b
  end
end

foo

__END__
# Classes
class Object
  private
  def foo: -> ([:a | untyped, :b | untyped])
end
