def log
  nn = nil
  ary = [1, "str"].to_enum.map do |n|
    nn = n
    n.to_s
  end
  return nn, ary
end

__END__
# Classes
class Object
  private
  def log: -> ([(Integer | String)?, Array[String]])
end
