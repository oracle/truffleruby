def foo(ary1, ary2)
  ary1.each do |x|
    ary2[0] = x
  end
  ary2
end

foo([1], ["str"])

__END__
# Classes
class Object
  private
  def foo: ([Integer] ary1, [String] ary2) -> ([Integer | String])
end
