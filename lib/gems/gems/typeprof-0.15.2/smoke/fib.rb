class Fib
  def fib_loop(x)
    a, b = 0, 1
    while x > 0
      a, b = b, a+b
      x -= 1
    end
    a
  end

  def fib_rec(x)
    if x <= 1
      x
    else
      fib_rec(x-1) + fib_rec(x-2)
    end
  end
end

Fib.new.fib_loop(42)
Fib.new.fib_rec(42)

__END__
# Classes
class Fib
  def fib_loop: (Integer x) -> Integer
  def fib_rec: (Integer x) -> Integer
end
