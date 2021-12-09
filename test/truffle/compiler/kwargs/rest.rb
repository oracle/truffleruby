def foo(*x)
  a, b = x
  a + b
end

loop do
  foo(rand(100), rand(100))
end
