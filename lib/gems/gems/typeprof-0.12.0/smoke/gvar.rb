$foo = 1
$foo = "str"
def log(x); end
log($foo)

__END__
# Global variables
$foo: Integer | String

# Classes
class Object
  private
  def log: (Integer | String x) -> nil
end
