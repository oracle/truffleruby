def log(x)
end

log([1] + ["str"] + [2] + [:sym])

__END__
# Classes
class Object
  private
  def log: (Array[:sym | Integer | String] x) -> nil
end
