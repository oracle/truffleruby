# This method call invokes (bottom type).boo, so the analysis may terminate
# Currently, SymbolBlock translates bot receiver to any type
[].each(&:boo)

def foo
end

__END__
# Classes
class Object
  private
  def foo: -> nil
end
