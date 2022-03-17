class Foo
end

class Bar
  include Foo
end

__END__
# Errors
smoke/wrong-include2.rb:5: [warning] attempted to include/extend non-module; ignored

# Classes
class Foo
end

class Bar
end
