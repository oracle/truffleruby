V = Object.new
def V.foo
  # Currently, this call is ignored because the recv of the call is any
  # We may allow a call whose recv is any to invoke Kernel's methods
  p(1)
end

__END__
# Classes
class Object
  V: Object
end
