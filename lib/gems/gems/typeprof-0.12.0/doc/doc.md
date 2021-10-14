# TypeProf: A type analysis tool for Ruby code based on abstract interpretation

## Show me demo first

See [demo.md](demo.md).

## How to use TypeProf

Analyze app.rb:

```
$ typeprof app.rb
```

Analyze app.rb with sig/app.rbs that specifies some method types:

```
$ typeprof sig/app.rbs app.rb
```

Here is a typical use case:

```
$ typeprof sig/app.rbs app.rb -o sig/app.gen.rbs
```

Here is a list of currently avaiable options:

* `-o OUTFILE`: Write the analyze result to OUTFILE instead of standard output
* `-q`: Hide the progress indicator
* `-v`: Alias to `-fshow-errors`
* `-d`: Show the analysis log (Currently, the log is just for debugging and may become very huge)
* `-I DIR`: Add `DIR` to the file search path of `require`
* `-r GEMNAME`: Load the RBS files of `GEMNAME`
* `--exclude-dir DIR`: Omit the result of files that are placed under the directory `DIR`.  If there are some directory specifications, the latter one is stronger.  (Assuming that `--include-dir foo --exclude-dir foo/bar` is specified, the analysis result of foo/bar/baz.rb is omitted, but foo/baz.rb is shown.)
* `--include-dir DIR`: Show the result of files that are placed under the directory `DIR`.  If there are some directory specifications, the latter one is stronger.  (Assuming that `--exclude-dir foo --include-dir foo/bar` is specified, the analysis result of foo/bar/baz.rb is shown, but foo/baz.rb is omitted.)
* `--show-errors`: Prints out possible bugs found during execution (often a lot of false positives).
* `--show-untyped`: When TypeProf infers a type `A | untyped`, it simply outputs `A` by default. But this option forces to output `A | untyped`.
* `--type-depth-limit=NUM`: (undocumented yet)

## What is a TypeProf?

TypeProf is a Ruby interpreter that *abstractly* executes Ruby programs at the type level.
It executes a given program and observes what types are passed to and returned from methods and what types are assigned to instance variables.
All values are, in principle, abstracted to the class to which the object belongs, not the object itself (detailed in the next section).

Here is an example of a method call.

```
def foo(n)
  p n      #=> Integer
  n.to_s
end

p foo(42)  #=> String
```

The analysis results of TypeProf are as follows.

```
$ ruby exe/typeprof test.rb
# Revealed types
#  test.rb:2 #=> Integer
#  test.rb:6 #=> String

# Classes
class Object
  def foo : (Integer) -> String
end
```

When the method call `foo(42)` is executed, the type (abstract value) "`Integer`" is passed instead of the `Integer` object 42.
The method `foo` executes `n.to_s`.
Then, the built-in method `Integer#to_s` is called and you get the type "`String`", which the method `foo` returns.
Collecting observations of these execution results, TypeProf outputs, "the method `foo` receives `Integer` and returns `String`" in the RBS format.
Also, the argument of `p` is output in the `Revealed types` section.

Instance variables are stored in each object in Ruby, but are aggregated in class units in TypeProf.

```
class Foo
  def initialize
    @a = 42
  end

  attr_accessor :a
end

Foo.new.a = "str"

p Foo.new.a #=> Integer | String
```

```
$ ruby exe/typeprof test.rb
# Revealed types
#  test.rb:11 #=> Integer | String

# Classes
class Foo
  attr_accessor a : Integer | String
  def initialize : -> Integer
end
```


## Abstract values

As mentioned above, TypeProf abstracts almost all Ruby values to the type level, with some exceptions like class objects.
To avoid confusion with normal Ruby values, we use the word "abstract value" to refer the values that TypeProf handles.

TypeProf handles the following abstract values.

* Instance of a class
* Class object
* Symbol
* `untyped`
* Union of abstract values
* Instance of a container class
* Proc object

Instances of classes are the most common values.
A Ruby code `Foo.new` returns an instance of the class `Foo`.
This abstract value is represented as `Foo` in the RBS format, though it is a bit confusing.
The integer literal `42` generates an instance of `Integer` and the string literal `"str"` generates an instance of `String`.

A class object is a value that represents the class itself.
For example, the constants `Integer` and `String` has class objects.
In Ruby semantics, a class object is an instance of the class `Class`, but it is not abstracted into `Class` in TypeProf.
This is because, if it is abstracted, TypeProf cannot handle constant references and class methods correctly.

A symbol is an abstract value returned by Symbol literals like `:foo`.
A symbol object is not abstracted to an instance of the class `Symbol` because its concrete vgalue is often required in many cases, such as keyword argumetns, JSON data keys, the argument of `Module#attr_reader`, etc.
Note that some Symbol objects are handled as instances of the class `Symbol`, for example, the return value of `String#to_sym` and Symbol literals that contains interpolation like `:"foo_#{ x }"`.

`untyped` is an abstract value generated when TypeProf fails to trace values due to analysis limits or restrictions.
Any operations and method calls on `untyped` are ignored, and the evaluation result is also `untyped`.

A union of abstract values is a value that represents multiple possibilities.,
For (a bit artificial) example, the result of `rand < 0.5 ? 42 : "str"` is a union, `Integer | String`.

An instance of a container class, such as Array and Hash, is an object that contains other abstract values as elements.
At present, only Array, Enumerator and Hash are supported.
Details will be described later.

A Proc object is a closure produced by lambda expressions (`-> {... }`) and block parameters (`&blk`).
During the interpretation, these objects are not abstracted but treated as concrete values associated with a piece of code.
In the RBS result, they are represented by using anonymous proc type, whose types they accepted and returned.


## Execution

TypeProf is a kind of Ruby interpreter, but its execution order is quite different from Ruby semantics.

### Branch

When it executes a branch, both clauses are executed in parallel. It is unspecified which is evaluated first.

In the following example, the "then" clause assigns an `Integer` to the variable `x` and the "else" clause assigns a `String` to `x`.

```ruby
if rand <0.5
  x = 42
else
  x = "str"
end

p x #=> Integer | String
```

TypeProf first evaluates the conditional expression, then does both "then" and "else" clauses (we cannot tell which comes first), and after the branch, evaluates the method call to `p` with `Integer | String`.


### Restart

If you assign different abstract values to an instance variable, the order of execution may be more complicated.

```ruby
class Foo
  def initialize
    @x = 1
  end

  def get_x
    @x
  end

  def update_x
    @x = "str"
  end
end

foo = Foo.new

# ...

p foo.get_x #=> Integer | String

# ...

foo.update_x
```

In the above example, an `Integer` is assigned to the instance variable `@x` in `Foo#initialize`.
`Foo#get_x` reads `@x` and returns an `Integer` once.
However, when `Foo#update_x` is called, the abstract value of the instance variable `@x` is expanded to `Integer | String`.
Therefore, reading `@x` should have returned a `Integer | String` instead of a simple `Integer`, and the access to `@x` in `Foo#get_x` restarts to return `Integer | String`, i.e., retroactively executed again.
Therefore, the return value of the call to `Foo#get_x` will eventually be `Integer | String`.


### Method call

TypeProf does not keep track of the call stack.
In other words, there is no concept of "caller" during the execution of the method.
Instead, when a method returns, it returns the abstract value to all possible places that may invoke to the method.

```
def fib(n)
  if n <2
    return n
  else
    fib(n-1) + fib(n-2)
  end
end

p fib(10) #=> Integer
```

In the above example, the method `fib` is called from three places (including recursive calls).
When `return n` is executed, TypeProf returns an `Integer` to all three places.
Note that, in Ruby, we cannot statically identify all places that may call to the method (because it depends upon the type of receiver).
Therefore, if TypeProf finds a new call to `fib` after `return n` is executed, the call also returns an `Integer` immediately.
If a method returns different abstract values, it can lead to retrospective execution.


### Stub execution

Even after TypeProf traced all programs as possible, there may be methods or blocks that aren't executed.
For example, a method is not executed if it is called from nowhere; this is typical for library method that has no test.
(Basically, when you use TypeProf, it is recommended to invoke all methods with supposed argument types.)
TypeProf forcibly calls these unreachable methods and blocks with `untyped` as arguments.

```
def foo(n)
end

def bar(n)
  foo(1)
end
```

In the above program, neither the method `foo` nor the method `bar` is called.
TypeProf stub-calls the `bar` with a `untyped` arugment, so you can get the information that an `Integer` is passed to a method `foo`.

However, this feature may slow down the analysis and may also brings many wrong guesses, so we plan to allow a user to enable/disable this feature in the configuration.


## Limitations

Some Ruby language features cannot be handled because they abstract values.

Basically, it ignores language features whose object identity is important, such as singleton methods for general objects.
Note that class method definitions are handled correctly; class objects are not abstracted for the sake.
Currently, TypeProf only handles instance methods and class methods; it has no general concept of metaclasses (a class of a class).

Meta programming is only partially supported.

* `Module#attr_reader` and `Object#send` handle correctly only when symbol abstract value is passed (for example, when written in a symbol literal).
* `Kernel#instance_eval` only supports the function to replace the receiver object when a block is passed (the contents of the string are not tracked).
* `Class.new` is not supported; it always returns `untyped`.
* `Kernel#require` has a dedicated support only when the argument string is a literal.


## Other features

### Partial RBS specification

Sometimes, TypeProf fails to correctly infer the programer's intent due to theoretical or implementation limitations.
In such cases, you can manually write a RBS description for some difficult methods to convey your intent to TypeProf.

For example, TypeProf does not handle a overloaded method.

```
# Programmer Intent: (Integer) -> Integer | (String) -> String
# TypeProf         : (Integer | String) -> (Integer | String)
def foo(n)
  if n.is_a?(Integer)
    42
  else
    "str"
  end
end

# Overload intent not respected
p foo(42) #=> Integer | String
p foo("str") #=> Integer | String
```

Assume that a programmer write the method `foo` as a overloaded method that returns an `Integer` only when an `Integer` is passed, and that returns a `String` only when a `String` is passed.
Thus, we expect the result of `foo(42)` to be an `Integer`. However, it's a bit wider result, `Integer | String`.

If you write the RBS manually to specify the intention of the method `foo`, the result will be as intended.

```
# test.rbs
class Object
  def foo: (Integer) -> Integer | (String) -> String
end
```

```
# test.rb
def foo(n)
  # Regardless of the contents, the description of test.rbs has priority
end

# Overload is respected correctly
p foo(42) #=> Integer
p foo("str") #=> String
```

Many of the built-in class methods are also specified by RBS.
We plan a feature to load all RBS files of libraries required in Gemfile (but not implemented yet).

RBS's "interface" type is not supported and is treated as `untyped`.

### Debug feature

Unfortunately, understanding the behavior and analysis results of TypeProf is sometimes difficult.

Currently, you can observe the abstract value of the argument by calling `Kernel#p` in your code, as if you debug your program in Ruby.
The only way to get a deeper understanding of the analysis is to watch the debug output with the environment variable `TP_DEBUG=1`.
We plan to provide some more useful way to make it easy to understand the analysis result in the future.


### Flow-sensitive analysis

TypeProf attempts to separate branches if the condition separates a union abstract value.
For example, consider that a local variable `var` has an abstract value `Foo | Bar`, and that a branch condition is `var.is_a?(Foo)`.
TypeProf will execute the "then" clause with `var` as only a `Foo`, and does the "else" clause with `var` as only a `Bar`.

Note that it can work well only if the receiver is a local variable defined in the current scope.
If the condition is about an instance variable, say `@var.is_a?(Foo)`, or if the variable `var` is defined outside the block, the union is not separated.
At present, only the following simple patterns (`is_a?`, `respond_to?`, and `case`/`when`) can be handled well.

```
def foo(x)
  if x.is_a?(Integer)
    p x #=> Integer
  else
    p x #=> String
  end
end

foo(42)
foo("str")
```

```
def foo(x)
  if x.respond_to?(:times)
    p x #=> Integer
  else
    p x #=> String
  end
end

foo(42)
foo("str")
```

```
def foo(x)
  case x
  when Integer
    p x #=> Integer
  when String
    p x #=> String
  end
end

foo(42)
foo("str")
```


### Container type

At present, only Array-like containers (Array and Enumerator) and Hash-like containers (Hash) are supported.

TypeProf keeps the object identity inside a method; the container instances are identified by the place where it is created.
You can update the types; this allows the following code to initialize the array:

```
def foo
  a = []

  100.times {|n| a << n.to_s}

  a
end

p foo #=> Array[String]
```

However, we do not track updates across methods (due to performance reasons).

```
def bar(a)
  a << "str"
end

def foo
  a = []

  bar(a)

  a
end

foo #=> [], not Array[String]
```

When a container abstract value is read from an instance variable, an update operation against it will be respected to the instance variable.

Currently, TypeProf has some limitations about container instances (because of performance).

* If you put a container type into a key of hash object, the key is replaced with `untyped`.
* The maximam depth of nested arrays and hashs is limited to 5.

We plan to allow them to be configurable, and relax the depth limitation when RBS is manually speficied (mainly for JSON data).


### (Write later)

* Proc
* Struct
