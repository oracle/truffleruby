class Object
  def foo
    # The receiver is considered as an empty array
    self[0]
  end
end

# The elements are dropped when it is passed as a receiver (to avoid state explosion)
[0].foo

__END__
# Classes
class Object
  def foo: -> nil
end
