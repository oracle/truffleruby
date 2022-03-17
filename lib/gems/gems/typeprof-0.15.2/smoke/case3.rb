def foo
  case
  when *[]
    return 1
  when false
    return 1.0
  when rand < 0.5
    return "str"
  end
end

__END__
# Classes
class Object
  private
  def foo: -> ((Integer | String)?)
end
