def foo
  unknown_method
end

def bar
  foo
end

def baz
  bar
end

baz

# smoke/backtrace.rb:2: [error] undefined method: Object#unknown_method
#         from smoke/backtrace.rb:6
#         from smoke/backtrace.rb:10
#         from smoke/backtrace.rb:13
# Object#baz :: () -> any
# Object#bar :: () -> any
# Object#foo :: () -> any

__END__
# Errors
smoke/backtrace.rb:2: [error] undefined method: Object#unknown_method

# Classes
class Object
  private
  def foo: -> untyped
  def bar: -> untyped
  def baz: -> untyped
end
