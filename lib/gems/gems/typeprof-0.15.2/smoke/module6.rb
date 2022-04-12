module A
end

module B
  include A
end

module A
  include B
end

class C
  include A
end

def log
  C.new.foo
end

__END__
# Errors
smoke/module6.rb:17: [error] undefined method: C#foo

# Classes
class Object
  private
  def log: -> untyped
end

module A
  include B
end

module B
  include A
end

class C
  include A
end
