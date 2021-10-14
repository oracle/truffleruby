def number?(ty)
  %w[integer float].include?(ty).then { puts 1 }
end
number?('string')

__END__
# Classes
class Object
  private
  def number?: (String ty) -> nil
end
