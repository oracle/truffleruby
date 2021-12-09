def bar(a:, b:)
  a + b
end

def foo(a, b)
  send(:bar, a: a, b: b)
end

loop do
  foo(rand(100), rand(100))
end
