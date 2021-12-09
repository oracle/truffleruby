def foo(**x)
  a = x[:a]
  b = x[:b]
  a + b
end

loop do
  foo(a: rand(100), b: rand(100))
end
