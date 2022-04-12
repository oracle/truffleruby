def foo(*r, k:)
end

a = [1, 2, 3]
h = { k: 42 }
foo(*a, **h)

__END__
# Classes
class Object
  private
  def foo: (*Integer | {k: Integer} r, k: Integer) -> nil
end
