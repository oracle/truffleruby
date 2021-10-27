# truffleruby_primitives: true

class Fiber
  def self.current
    Primitive.fiber_current
  end
end
