def foo
  begin
  rescue
    break 42
  end while true
end

__END__
# Classes
class Object
  private
  def foo: -> Integer
end
