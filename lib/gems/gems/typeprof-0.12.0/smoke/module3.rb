module M1
  def foo
    :m1
  end
end

module M2
  def foo
    :m2
  end
end

class C
  include M1
  include M2
end

C.new.foo

__END__
# Classes
module M1
  def foo: -> :m1
end

module M2
  def foo: -> :m2
end

class C
  include M2
  include M1
end
