# % jt --use master graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/short-callee.rb                       # => |Tier 2|Time   347( 142+205 )ms|AST   35|Inlined   0Y   0N|IR    159/   299|CodeSize    1158|Timestamp 275177231878143|Src short-callee.rb:4
# % jt --use call-target-agnostic-kwargs graph --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/short-callee.rb  # => |Tier 2|Time   242( 116+126 )ms|AST   24|Inlined   0Y   0N|IR     44/    98|CodeSize     422|Timestamp 275197152193913|Src short-callee.rb:4

def foo(a:, b:)
  a + b
end

loop do
  foo(a: rand(100), b: rand(100))
end
