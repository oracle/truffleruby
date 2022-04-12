module M2
  def foo
    :m2_foo
  end
end

module M1
  extend M2

  def foo
    :m1_foo
  end
end

class C
  extend M1
end

M1.foo
C.foo
__END__
# Classes
module M2
  def foo: -> :m2_foo
end

module M1
  extend M2

  def foo: -> :m1_foo
end

class C
  extend M1
end
