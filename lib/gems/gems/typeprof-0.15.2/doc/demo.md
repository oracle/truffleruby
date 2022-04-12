# TypeProf demo cases

## A simple demo with a "User" class

```ruby
def hello_message(user)
  "The name is " + user.name
end

def type_error_demo(user)
  "The age is " + user.age
end

user = User.new(name: "John", age: 20)

hello_message(user)
type_error_demo(user)
```

```ruby
class User
  attr_reader name: String
  attr_reader age: Integer

  def initialize: (name: String, age: Integer) -> void
end
```

Result:
```
$ typeprof test.rb test.rbs
# Errors
test.rb:6: [error] failed to resolve overload: String#+

# Classes
class Object
  def hello_message : (User) -> String
  def type_error_demo : (User) -> untyped
end
```

You can [try this analysis online](https://mame.github.io/typeprof-playground/#rb=def+hello_message%28user%29%0A++%22The+name+is+%22+%2B+user.name%0Aend%0A%0Adef+type_error_demo%28user%29%0A++%22The+age+is+%22+%2B+user.age%0Aend%0A%0Auser+%3D+User.new%28name%3A+%22John%22%2C+age%3A+20%29%0A%0Ahello_message%28user%29%0Atype_error_demo%28user%29&rbs=class+User%0A++attr_reader+name%3A+String%0A++attr_reader+age%3A+Integer%0A%0A++def+initialize%3A+%28name%3A+String%2C+age%3A+Integer%29+-%3E+void%0Aend).

## A simple demo to generate the signature prototype of "User" class

```ruby
class User
  def initialize(name:, age:)
    @name, @age = name, age
  end
  attr_reader :name, :age
end

# A test case to tell TypeProf what types are expected by the class and methods
User.new(name: "John", age: 20)
```

Result:
```
$ typeprof -v test.rb
# Classes
class User
  attr_reader name : String
  attr_reader age : Integer
  def initialize : (name: String, age: Integer) -> [String, Integer]
end
```

## Type inspection by `p` (`Kernel#p`)

```ruby
p 42          #=> Integer
p "str"       #=> String
p "str".chars #=> Array[String]
```

Result:
```
$ typeprof test.rb
# Revealed types
#  test.rb:1 #=> Integer
#  test.rb:2 #=> String
#  test.rb:3 #=> Array[String]
```

## Block with builtin methods

```ruby
# TypeProf runs this block only once
10000000000000.times do |n|
  p n #=> Integer
end

# "each" with Heterogeneous array yields a union type
[1, 1.0, "str"].each do |e|
  p e #=> Float | Integer | String
end

# You can use the idiom `&:method_name` too
p [1, 1.0, "str"].map(&:to_s) #=> Array[String]
```

## User-defined blocks

```ruby
def foo(n)
  yield n.to_s
end

foo(42) do |n|
  p n #=> String
  nil
end
```

Result:
```
$ typeprof test.rb
# Revealed types
#  test.rb:6 #=> String

# Classes
class Object
  def foo : (Integer) { (String) -> nil } -> nil
end
```

## Arrays

```ruby
# A fixed-length array literal generates a "tuple" array
ary = [1, 1.0]

# A tuple array keeps its length, and the association between indexes and elements
p ary    #=> [Integer, Float]
p ary[0] #=> Integer
p ary[1] #=> Float

# Destructive operation is well handled (in method-local analysis)
ary[0] = "str"
p ary #=> [String, Float]

# An calculated array generates a "sequence" array
ary = [1] + [1.0]

# A sequence array does not keep length nor association
p ary    #=> Array[Float | Integer]
p ary[0] #=> Float | Integer

# Destructive operation is still handled (but "weak update" is applied)
ary[0] = "str"
p ary #=> Array[Float | Integer | String]
```

## Multiple return values by using a tuple array

```ruby
def foo
  return 42, "str"
end

int, str = foo
p int #=> Integer
p str #=> String
```

## Delegation by using a tuple array

```ruby
def foo(x, y, z)
end

def proxy(dummy, *args)
  foo(*args)
end

proxy(:dummy, 1, 1.0, "str")
```

## Symbols

```ruby
# Symbols are handled as concrete values instead of abstract ones
p [:a, :b, :c] #=> [:a, :b, :c]
```

## Hashes

```ruby
# A Hash is a "type-to-type" map
h = { "int" => 1, "float" => 1.0 }
p h        #=> {String=>Float | Integer}
p h["int"] #=> Float | Integer

# Symbol-key hashes (a.k.a. records) can have distinct types for each key as Symbols are concrete
h = { int: 1, float: 1.0 }

p h         #=> {:int=>Integer, :float=>Float}
p h[:int]   #=> Integer
p h[:float] #=> Float

# Symbol-key hash can be appropriately passed to a keyword method
def foo(int:, float:)
  p [int, float] #=> [Integer, Float]
end

foo(**h)
```

## Structs

```ruby
FooBar = Struct.new(:foo, :bar)

obj = FooBar.new(42)
obj.foo = :dummy
obj.bar = "str"
```

Result:
```
$ typeprof test.rb
# Classes
class FooBar < Struct
  attr_accessor foo() : :dummy | Integer
  attr_accessor bar() : String?
end
```

## Exceptions

```ruby
# TypeProf assumes that any exception may be raised anywhere
def foo
  x = 1
  x = "str"
  x = :sym
ensure
  p(x) #=> :sym | Integer | String
end
```

Result:
```
$ typeprof test.rb
# Revealed types
#  test.rb:6 #=> :sym | Integer | String

# Classes
class Object
  def foo : -> :sym
end
```

## RBS overloaded methods

```ruby
# TypeProf selects all overloaded method declarations that matches actual arguments
p foo(42)    #=> Integer
p foo("str") #=> String
p foo(1.0)   #=> failed to resolve overload: Object#foo
```

```
class Object
  def foo: (Integer) -> Integer
         | (String) -> String
end
```

## Flow-sensitive analysis demo: case/when with class constants

```ruby
def foo(n)
  case n
  when Integer
    p n #=> Integer
  when String
    p n #=> String
  else
    p n #=> Float
  end
end

foo(42)
foo(1.0)
foo("str")
```

Result:
```
$ typeprof test.rb
# Revealed types
#  test.rb:4 #=> Integer
#  test.rb:8 #=> Float
#  test.rb:6 #=> String

# Classes
class Object
  def foo : (Float | Integer | String) -> (Float | Integer | String)
end
```

## Flow-sensitive analysis demo: `is_a?` and `respond_to?`

```ruby
def foo(n)
  if n.is_a?(Integer)
    p n #=> Integer
  else
    p n #=> Float | String
  end

  if n.respond_to?(:times)
    p n #=> Integer
  else
    p n #=> Float | String
  end
end

foo(42)
foo(1.0)
foo("str")
```

## Flow-sensitive analysis demo: `x || y`

```ruby
# ENV["FOO"] returns String? (which means String | nil)
p ENV["FOO"]              #=> String?

# Using "|| (default value)" can force it to be non-nil
p ENV["FOO"] || "default" #=> String
```

## Recursion

```ruby
def fib(x)
  if x <= 1
    x
  else
    fib(x - 1) + fib(x - 2)
  end
end

fib(40000)
```

Result:
```
$ typeprof test.rb
# Classes
class Object
  def fib : (Integer) -> Integer
end
```

## "Stub-execution" that invokes methods without tests

```ruby
def foo(n)
  # bar is invoked with Integer arguments
  bar(42)
  n
end

def bar(n)
  n
end

# As there is no test code to call methods foo and bar,
# TypeProf tries to invoke them with "untyped" arguments
```

Result:
```
$ typeprof test.rb
# Classes
class Object
  def foo : (untyped) -> untyped
  def bar : (Integer) -> Integer
end
```

## Library demo

```ruby
require "pathname"

p Pathname("foo")         #=> Pathname
p Pathname("foo").dirname #=> Pathname
p Pathname("foo").ctime   #=> Time
```

## More

See ruby/typeprof's [smoke](https://github.com/ruby/typeprof/tree/master/smoke) directory.
