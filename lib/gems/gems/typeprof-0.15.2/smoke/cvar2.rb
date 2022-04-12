class A
  @@var = :a
end

class B < A
  @@var = :b
end

__END__
# Classes
class A
  @@var: :a
end

class B < A
  @@var: :b
end
