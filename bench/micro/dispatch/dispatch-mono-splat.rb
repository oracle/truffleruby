class Callee
  def call(arg1 = nil, arg2 = nil, arg3 = nil, arg4 = nil)
    :foo
  end
end

callees = Array.new(1000) { Callee.new }
args = Array.new(1000) { |i| Array.new(i % 4, 1) }

benchmark 'dispatch-mono-splat' do
  i = 0
  while i < 1000
    callees[i].call(*args[i])
    i += 1
  end
end
