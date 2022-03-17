def log(x)
end

[1, "str", :sym].each do |x|
  log(x)
end

log(nil)

__END__
# Classes
class Object
  private
  def log: ((:sym | Integer | String)? x) -> nil
end
