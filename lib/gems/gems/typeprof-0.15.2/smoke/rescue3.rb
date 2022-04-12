Ary = [1, "str"]

def foo
  Ary[
    begin
      0
    rescue
      1
    end
  ]
end

__END__
# Classes
class Object
  Ary: [Integer, String]

  private
  def foo: -> (Integer | String)
end
