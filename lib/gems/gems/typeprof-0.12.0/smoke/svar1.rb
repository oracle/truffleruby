def foo
  "str" =~ /(str)/
  [$&, $1]
end

foo

__END__
# Classes
class Object
  private
  def foo: -> [String?, String?]
end
