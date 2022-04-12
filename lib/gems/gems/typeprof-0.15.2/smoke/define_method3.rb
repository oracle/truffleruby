class Foo
  x = 1
  define_method(:foo) {
    @log = x
  }
end

__END__
# Classes
class Foo
  @log: Integer

# def foo: () -> void
end
