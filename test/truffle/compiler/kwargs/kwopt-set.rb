def foo(a:, b: 101)
  a + b
end

loop do
  foo(a: rand(100), b: rand(100))
end
