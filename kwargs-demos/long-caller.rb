# % jt --use master graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/long-caller.rb                        #=> |Tier 2|Time  1299( 414+885 )ms|AST   65|Inlined   0Y   1N|IR    147/  1728|CodeSize    6994|Timestamp 275000175730540|Src long-caller.rb:8
# % jt --use call-target-agnostic-kwargs graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/long-caller.rb   #=> |Tier 2|Time   468( 289+179 )ms|AST   65|Inlined   0Y   1N|IR    120/   230|CodeSize    1078|Timestamp 275068444181138|Src long-caller.rb:8

def bar(a:, b:, c:, d:, e:, f:, g:, h:)
  a + b + c + d + e + f + g + h
end

def foo(a, b, c, d, e, f, g, h)
  bar(a: a, b: b, c: c, d: d, e: e, f: f, g: g, h: h)
end

loop do
  foo(rand(100), rand(100), rand(100), rand(100), rand(100), rand(100), rand(100), rand(100))
end
