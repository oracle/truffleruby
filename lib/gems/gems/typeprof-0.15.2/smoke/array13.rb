def tuple_set
  ary = [:a, :b, :c]
  ary[-1] = :z
  ary
end

def tuple_get
  ary = [:a, :b, :c]
  ary[-1]
end

def seq_set
  ary = [:a, :b, :c] + []
  ary[-1] = :z
  ary
end

def seq_get
  ary = [:a, :b, :c] + []
  ary[-1]
end

__END__
# Classes
class Object
  private
  def tuple_set: -> [:a, :b, :z]
  def tuple_get: -> :c
  def seq_set: -> (Array[:a | :b | :c | :z])
  def seq_get: -> (:a | :b | :c)
end
