def get_module
  C.new.get_module
end

def get_module_foo
  C.new.get_module.foo
end

def get_interface
  C.new.get_interface
end

def get_interface_foo
  C.new.get_interface.foo
end

__END__
# Classes
class Object
  private
  def get_module: -> M
  def get_module_foo: -> Integer
  def get_interface: -> _I
  def get_interface_foo: -> Integer
end
