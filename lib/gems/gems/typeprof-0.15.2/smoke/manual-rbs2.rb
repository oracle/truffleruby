class A
  class B
    def foo
      unknown
    end
  end
end

def bar
  A::B.new.foo + 1
end

__END__
# Errors
smoke/manual-rbs2.rb:4: [error] undefined method: A::B#unknown

# Classes
class Object
  private
  def bar: -> untyped
end
