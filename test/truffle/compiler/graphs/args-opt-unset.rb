def foo(a, b=101)
  a + b
end

loop do
  foo(rand(100))
end
