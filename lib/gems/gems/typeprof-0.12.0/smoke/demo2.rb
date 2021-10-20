# override
def my_to_s(x)
  x.to_s
end

my_to_s(42)
my_to_s("str")
my_to_s(:sym)

__END__
# Classes
class Object
  private
  def my_to_s: (:sym | Integer | String x) -> String
end
