# I'm unsure how, but this code calls ContainerType::Elements.dummy_elements
# I should investigate the mechanism, but anyway I record this code

class Foo
  define_method(:f) {|**h| }
end

Foo.new.f

__END__
# Classes
class Foo
  def f: (**untyped) -> nil
end
