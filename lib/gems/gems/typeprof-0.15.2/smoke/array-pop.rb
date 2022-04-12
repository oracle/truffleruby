def foo
  [1, "str", :sym].pop()
end

foo

__END__
# Classes
class Object
  private
  def foo: -> (:sym | Integer | String)
end
