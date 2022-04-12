def cond1?
  rand < 0.5
end

def cond2?
  rand < 0.5
end

def foo
  while true
    if cond1? .. cond2?
      return 42
    end
    if cond1? .. cond2?
      return "str"
    end
  end
  return
end

__END__
# Classes
class Object
  private
  def cond1?: -> bool
  def cond2?: -> bool
  def foo: -> (Integer | String)
end
