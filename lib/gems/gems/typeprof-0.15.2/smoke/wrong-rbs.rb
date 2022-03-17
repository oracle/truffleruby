def foo
end
def bar(n)
end
def baz(n=1)
end
def qux(a,n=1)
end

__END__
# Errors
smoke/wrong-rbs.rb:1: [error] RBS says that the arity may be 1, but the method definition requires at most 0 arguments
smoke/wrong-rbs.rb:3: [error] RBS says that the arity may be 0, but the method definition requires at least 1 arguments

# Classes
