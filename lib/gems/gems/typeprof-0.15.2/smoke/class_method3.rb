class ForWin
end
class ForLinux
end

v = RUBY_PLATFORM =~ /windows/ ? ForWin : ForLinux

def v.foo
end

if RUBY_PLATFORM =~ /windows/
  v = ForWin
else
  v = ForLinux
end

v.foo

__END__
# Classes
class ForWin
  private
  def self.foo: -> nil
end

class ForLinux
  private
  def self.foo: -> nil
end
