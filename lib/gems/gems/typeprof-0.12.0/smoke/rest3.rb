string_array = ["str"] + ["str"]

def foo(*r)
end
foo(*string_array)

def bar(*r)
end
bar(1, *string_array)

def baz(x, *r)
end
baz(*string_array)

def qux(x, *r)
end
qux(1, *string_array)

def corge(*r, z)
end
corge(1, *string_array)

def grault(a, o=1, *r, z)
end
grault(*string_array)

__END__
# Classes
class Object
  private
  def foo: (*String r) -> nil
  def bar: (*Integer | String r) -> nil
  def baz: (String x, *String r) -> nil
  def qux: (Integer x, *String r) -> nil
  def corge: (*Integer | String r, Integer | String z) -> nil
  def grault: (String a, ?Integer | String o, *String r, String z) -> nil
end
