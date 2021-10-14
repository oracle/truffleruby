def log(n)
end

log(
  begin
    1
  rescue
    "str"
  end
)

__END__
# Classes
class Object
  private
  def log: (Integer | String n) -> nil
end
