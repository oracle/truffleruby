def foo
  { int: 1, str: "str" }
end

foo

def bar
  # This returns {Integer=>Integer | String, String=>String} but RBS cannot express it
  { 1 => 1, 2 => "str", "s" => "s" }
end

bar

__END__
# Classes
class Object
  private
  def foo: -> {int: Integer, str: String}
  def bar: -> (Hash[Integer | String, Integer | String])
end
