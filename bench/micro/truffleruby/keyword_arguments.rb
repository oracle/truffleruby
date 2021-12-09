# frozen_string_literal: true

require 'benchmark/ips'
puts RUBY_DESCRIPTION

# rubocop:disable Metrics/ParameterLists

class A 
  def keyword_with_defaults(a: 1, b: 2, c: 3)
    a + b + c
  end

  def keyword_without_defaults(a:, b:, c:)
    a + b + c
  end

  def positional_without_defaults(a, b, c)
    a + b + c
  end
end

class B
  def keyword_with_defaults(c: 18, b: 19, a: 20)
    a + b + c
  end

  def keyword_without_defaults(c:, b:, a:)
    a + b + c
  end

  def positional_without_defaults(a, b, c)
    a + b + c
  end
end

obj_a = A.new
obj_b = B.new
random = Random.new

Benchmark.ips do |x|
  x.report('Monomorphic, keyword with default args') do
    obj_a.keyword_with_defaults
  end

  x.report('Monomorphic, keyword without default args') do
    obj_a.keyword_without_defaults(a: 1, b: 2, c: 3)
  end

  x.report('Monomorphic, positional without default args') do
    obj_b.positional_without_defaults(1, 2, 3)
  end

  x.report('Polymorphic, keyword with default args') do
    (random.rand(2) == 0 ? A.new : B.new).keyword_with_defaults
  end

  x.report('Polymorphic, keyword without default args') do
    (random.rand(2) == 0 ? A.new : B.new).keyword_without_defaults(a: 1, b: 2, c: 3)
  end

  x.report('Polymorphic, positional without default args') do
    (random.rand(2) == 0 ? A.new : B.new).positional_without_defaults(1, 2, 3)
  end
end
