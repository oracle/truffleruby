class Foo
  def []=(*args)
    ary = []
    val = args[-1]
    ary[*args[0..-2]] = val # TODO: there is room to improve array_aset
    ary                     # This should be Array[String]
  end
end

Foo.new[1] = "str"

__END__
# Classes
class Foo
  def []=: (*Integer | String args) -> Array[bot]
end
