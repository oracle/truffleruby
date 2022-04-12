module M1
  def f(m); super :M1; end
end
module M2
  def f(m); super :M2; end
end
class C
  def f(m); end
end
class D < C
  def f(m); super :D; end
  include M1
end
class E < D
  def f(m); super :E; end
  include M2
end

E.new.f(:top)

__END__
# Classes
module M1
  def f: (:D m) -> nil
end

module M2
  def f: (:E m) -> nil
end

class C
  def f: (:M1 m) -> nil
end

class D < C
  include M1

  def f: (:M2 m) -> nil
end

class E < D
  include M2

  def f: (:top m) -> nil
end
