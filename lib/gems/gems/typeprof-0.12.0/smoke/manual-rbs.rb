def foo(n)
  case n
  when Integer
    n + 1
  when String
    n + "STR"
  else
    raise
  end
end

def ret_int
  foo(42)
end

def ret_str
  foo("str")
end

__END__
# Classes
class Object
# def foo: (Integer) -> Integer
#        | (String) -> String

  private
  def ret_int: -> Integer
  def ret_str: -> String
end
