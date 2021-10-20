def swap(a)
  [a[1], a[0]]
end
a = [42, "str"]
swap(a)

__END__
# Classes
class Object
  private
  def swap: ([Integer, String] a) -> [String, Integer]
end
