def foo(&b)
  b = 1
  b
end

foo { }

__END__
# Classes
class Object
  private
  def foo: { -> nil } -> Integer
end
