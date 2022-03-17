module M1
  def foo
    :foo
  end
end

module M2
  include M1
end

class C
  include M2
end

C.new.foo
__END__
# Classes
module M1
  def foo: -> :foo
end

module M2
  include M1
end

class C
  include M2
end
