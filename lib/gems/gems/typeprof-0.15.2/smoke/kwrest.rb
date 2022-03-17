class Foo
  def foo(**r)
    @r = r["foo".to_sym][42]
  end
end

__END__
# Classes
class Foo
  @r: Integer

# def foo: (**Integer) -> void
end
