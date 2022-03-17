# The following "break" is compiled as "throw" instruction since https://github.com/ruby/ruby/commit/34bc8210ed1624dc6ba24afef4616baa5a934df9
def foo
  begin
    while true
      break 1
    end
  rescue
    "foo"
  end
end

__END__
# Classes
class Object
  private
  def foo: -> (Integer | String)
end
