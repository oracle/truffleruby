p(1)
p("str")
p(:sym)
p([1, "str", :sym])

__END__
# Revealed types
#  smoke/reveal.rb:1 #=> Integer
#  smoke/reveal.rb:2 #=> String
#  smoke/reveal.rb:3 #=> :sym
#  smoke/reveal.rb:4 #=> [Integer, String, :sym]

# Classes
