class App
  FooBar = Struct.new(:foo, :bar)
end

App::FooBar.new(1, "str")

__END__
# Classes
class App
  class FooBar < Struct[untyped]
    attr_accessor foo(): Integer
    attr_accessor bar(): String
  end
end
