# RUBY_VERSION >= 3.0

def foo
  case [:a, :b, :c]
  in [a, b, :c]
    # Due to very subtle detail of bytecode, the variables "a" and "b" could be nil
    return a, b
  end
end

foo

__END__
# Errors
smoke/pattern-match1.rb:5: [error] undefined method: nil#length
smoke/pattern-match1.rb:5: [error] undefined method: nil#[]
smoke/pattern-match1.rb:5: [error] undefined method: nil#[]
smoke/pattern-match1.rb:5: [error] undefined method: nil#[]

# Classes
class Object
  private
  def foo: -> ([:a | untyped, :b | untyped])
end
