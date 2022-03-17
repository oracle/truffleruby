$foo = 1

def log
  $foo
end

__END__
# Errors
smoke/gvar2.rb:1: [warning] inconsistent assignment to RBS-declared variable

# Classes
class Object
  private
  def log: -> String
end
