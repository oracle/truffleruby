# % jt --use master graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/short-caller.rb                       # => |Tier 2|Time   699( 312+388 )ms|AST   28|Inlined   0Y   1N|IR     98/   463|CodeSize    1714|Timestamp 275223002067268|Src short-caller.rb:8
# % jt --use call-target-agnostic-kwargs graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/short-caller.rb  # => |Tier 2|Time   490( 281+210 )ms|AST   29|Inlined   0Y   1N|IR     90/   187|CodeSize     754|Timestamp 275261009530057|Src short-caller.rb:8

def bar(a:, b:)
  a + b
end

def foo(a, b)
  bar(a: a, b: b)
end

loop do
  foo(rand(100), rand(100))
end
