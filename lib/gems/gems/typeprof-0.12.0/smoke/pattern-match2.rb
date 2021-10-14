# RUBY_VERSION >= 3.0

def foo
  case { a: :A, b: :B, c: :C }
  in { a:, b: bb, c: :C }
    return a, bb
  end
end

foo
__END__
# Classes
class Object
  private
  def foo: -> [:A, :B]
end
