def gvar_test
  $gvar
end

class Foo
  def const_test
    CONST
  end

  def ivar_test
    @ivar
  end

  def cvar_test
    @@cvar
  end

  def self.cvar_test2
    @@cvar
  end
end

__END__
# Global variables
#$gvar: :gvar_example

# Classes
class Object
  private
  def gvar_test: -> :gvar_example
end

class Foo
  def const_test: -> :const_example
  def ivar_test: -> :ivar_example
  def cvar_test: -> :cvar_example
  def self.cvar_test2: -> :cvar_example
end
