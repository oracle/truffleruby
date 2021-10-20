a = ""
a.instance_eval do
  @a = 1
end

__END__
# Classes
class String
  @a: Integer
end
