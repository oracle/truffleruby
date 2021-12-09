# frozen_string_literal: true

require 'benchmark/ips'
puts RUBY_DESCRIPTION

# rubocop:disable Metrics/ParameterLists

class A 
  def keyword_with_defaults(a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9, j: 10)
    a + b + c + d + e + f + g + h + i + j
  end

  def keyword_without_defaults(a:, b:, c:, d:, e:, f:, g:, h:, i:, j:)
    a + b + c + d + e + f + g + h + i + j
  end

  def positional_without_defaults(a, b, c, d, e, f, g, h, i, j)
    a + b + c + d + e + f + g + h + i + j
  end
end

class B
  def keyword_with_defaults(j: 11, i: 12, h: 13, g: 14, f: 15, e: 16, d: 17,
    c: 18, b: 19, a: 20)
      a + b + c + d + e + f + g + h + i + j
  end

  def keyword_without_defaults(j:, i:, h:, g:, f:, e:, d:, c:, b:, a:)
    a + b + c + d + e + f + g + h + i + j
  end

  def positional_without_defaults(a, b, c, d, e, f, g, h, i, j)
    a + b + c + d + e + f + g + h + i + j
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
    obj_a.keyword_without_defaults(a: 1, b: 2, c: 3, d: 4, e: 5, 
      f: 6, g: 7, h: 8, i: 9, j: 10)
  end

  x.report('Monomorphic, positional without default args') do
    obj_b.positional_without_defaults(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  end

  x.report('Polymorphic, keyword with default args') do
    (random.rand(2) == 0 ? A.new : B.new).keyword_with_defaults
  end

  x.report('Polymorphic, keyword without default args') do
    (random.rand(2) == 0 ? A.new : B.new).keyword_without_defaults(a: 1, b: 2, c: 3, d: 4, e: 5, 
      f: 6, g: 7, h: 8, i: 9, j: 10)
  end

  x.report('Polymorphic, positional without default args') do
    (random.rand(2) == 0 ? A.new : B.new).positional_without_defaults(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  end
end
