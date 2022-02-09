class Callee
  def call(*args)
    :foo
  end
end

callees = Array.new(1000) { Callee.new }
args = Array.new(1000) { |i| Array.new(i % 4, 1) }

benchmark 'dispatch-mono-splat-rest' do
  i = 0
  while i < 1000
    callees[i].call(*args[i])
    i += 1
  end
end
