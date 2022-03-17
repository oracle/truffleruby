def foo
  if rand < 0.5
    unknown
  else
    [:a, [:b, :c]]
  end
end

a, (b, c) = foo

p(a)
__END__
# Errors
smoke/expandarray1.rb:3: [error] undefined method: Object#unknown

# Revealed types
#  smoke/expandarray1.rb:11 #=> :a | untyped

# Classes
class Object
  private
  def foo: -> ([:a, [:b, :c]] | untyped)
end
