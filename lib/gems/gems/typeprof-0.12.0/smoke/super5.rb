module M
  def f(m); super :M; end
end
class C
  def f(m); end
end
class D < C
  def f(m); super :D; end
  include M
end
class E < D
  def f(m); super :E; end
  include M
end

E.new.f(:top)

__END__
# Classes
module M
  def f: (:D | :E m) -> nil
end

class C
  def f: (:M m) -> nil
end

class D < C
  include M

  def f: (:M m) -> nil
end

class E < D
  include M

  def f: (:top m) -> nil
end
