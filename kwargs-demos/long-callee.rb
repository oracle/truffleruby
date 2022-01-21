# % jt --use master graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/long-callee.rb                        #=> |Tier 2|Time  1056( 106+951 )ms|AST  137|Inlined   0Y   0N|IR    711/  1523|CodeSize    5970|Timestamp 274182618136092|Src long-callee.rb:4
# % jt --use call-target-agnostic-kwargs graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/long-callee.rb   #=> |Tier 2|Time   266( 128+138 )ms|AST   72|Inlined   0Y   0N|IR     92/   152|CodeSize     570|Timestamp 274521828146975|Src long-callee.rb:4

def foo(a:, b:, c:, d:, e:, f:, g:, h:)
  a + b + c + d + e + f + g + h
end

loop do
  foo(a: rand(100), b: rand(100), c: rand(100), d: rand(100), e: rand(100), f: rand(100), g: rand(100), h: rand(100))
end
