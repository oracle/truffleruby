# Migration from JRuby to TruffleRuby

## Deployment

If you are migrating from JRuby probably the easiest way to use TruffleRuby is
via [GraalVM](doc/user/installing-graalvm.md), which gives you a JVM,
JavaScript, Ruby and other languages in one package.

If you don't need the Java interop capabilities of TruffleRuby then you could
also install via your [Ruby manager/installer](doc/user/ruby-managers.md)
as any other implementation of Ruby.

You can also use the
[standalone distribution](doc/user/standalone-distribution.md) as a simple
binary tarball. The binary tarball also doesn't allow for Java interop.

## Using Ruby from Java

JRuby supports many different ways to embed Ruby in Java, including JSR 223
(also know as `javax.script`), the Bean Scripting Framework (BSF), JRuby Embed
(also known as Red Bridge), and the JRuby direct embedding API.

TruffleRuby is only embedded via the Polyglot API, which is part of GraalVM. You
will need to use the GraalVM to use this API. See the [polyglot](polyglot.md)
documentation for more information about how to use Ruby from other languages
including Java - this document only shows the comparison to JRuby.

### Creating a context

In JRuby with JSR 223 where you would have written:

```java
ScriptEngineManager m = new ScriptEngineManager();
ScriptEngine scriptEngine = m.getEngineByName("ruby");
```

Or with BSF where you would have written:

```java
BSFManager.registerScriptingEngine("jruby", "org.jruby.embed.bsf.JRubyEngine", null);
BSFManager bsfManager = new BSFManager();
```

Or with JRuby Embed where you would have written:

```java
ScriptingContainer container = new ScriptingContainer();
```

Or with the direct embedding API where you would have written:

```java
Ruby ruby = Ruby.newInstance(new RubyInstanceConfig());
```

In TruffleRuby you now write:

```java
Context polyglot = Context.newBuilder().allowAllAccess(true).build();
```

`allowAllAccess(true)` allows the permissive access permissions that Ruby needs
by default. GraalVM by default disallows many things which may not be safe, such
as native file access, but a normal Ruby installation uses these so we enable
them. You can use the option `ruby.platform.native` to disable the need for the
option, but this will restrict some of Ruby's functionality.

```java
Context polyglot = Context.newBuilder()
  .option("ruby.platform.native", "false")
  .build();
```

You would normally create your context inside a `try` block to ensure it is
properly disposed.

```java
try (Context polyglot = Context.newBuilder().allowAllAccess(true).build()) {
}
```

### Setting options

You can set TruffleRuby [options](options.md) via system properties, or via the
`.option(name, value)` builder method.

### Evaluating code

In JRuby where you would have written one of these:

```java
scriptEngine.eval("puts 'hello'");
bsfManager.exec("jruby", "<script>", 1, 0, "puts 'hello'");
container.runScriptlet("puts 'hello'");
ruby.evalScriptlet("puts 'hello'");
```

In TruffleRuby you now write this:

```java
polyglot.eval("ruby", "puts 'hello'");
```

Note that `eval` is language agnostic, so you need to specify the language each
time.

### Evaluating code with parameters

In JRuby with JSR 223 you can pass parameters, called bindings, into a script.

```java
Bindings bindings = scriptEngine.createBindings();
bindings.put("a", 14);
bindings.put("b", 2);
scriptEngine.eval("puts a + b", bindings);
```

In TruffleRuby the `eval` method does not take parameters. Instead you should
return a proc which does take parameters, and then call `execute` on this value.

```java
polyglot.eval("ruby", "lambda { |a, b| puts a + b }").execute(14, 2);
```

### Primitive values

The different embedding APIs handle primitive values in different ways. In JSR
223, BSF, and JRuby Embed, the return type is `Object` and can be cast to a
primitive like `long` and checked with `instanceof`. In the direct embedding API
the return is the root `IRubyObject` interface and you will need to convert a
primitive to an `Integer`, and from there to a Java `long`.

```java
(long) scriptEngine.eval("14 + 2");
(long) bsfManager.eval("jruby", "<script>", 1, 0, "14 + 2");
(long) container.runScriptlet("14 + 2");
ruby.evalScriptlet("14 + 2").convertToInteger().getLongValue();
```

In TruffleRuby the return value is always an encapsulated `Value` object, which
can be accessed as a `long` if that is possible for the object. `fitsInLong()`
can test this.

```java
polyglot.eval("ruby", "14 + 2").asLong();
```

### Calling methods

To call a method on an object you get from an `eval`, or any other object, in
the JRuby embedding APIs you either need to ask the context to invoke the
method, or in the cast of direct embedding you need to call a method on the
receiver and marshal the arguments into JRuby types yourself. I don't think
the BSF has a way to call methods.

```java
((Invocable) scriptEngine).invokeMethod(scriptEngine.eval("Math"), "sin", 2);
container.callMethod(container.runScriptlet("Math"), "sin", 2);
ruby.evalScriptlet("Math").callMethod(ruby.getCurrentContext(), "sin", new IRubyObject[]{ruby.newFixnum(2)})
```

In TruffleRuby the `Value` class has a `getMember` method to return Ruby methods
on an object, which you can then call by calling `execute`. You don't need to
marshal the arguments.

```java
polyglot.eval("ruby", "Math").getMember("sin").execute(2);
```

### Passing blocks

Blocks are a Ruby-specific language feature, so they don't appear in language
agnostic APIs like JSR 223 and BSF. The JRuby Embed API and direct embedding do
allow it, having a `Block` parameter to the `callMethod` method, but it's not
clear how you would create a `Block` object to use this.

In TruffleRuby you should return a Ruby lambda that performs your call, passing
a block that executes a Java lambda that you pass in.

```java
polyglot.eval("ruby", "lambda { |block| (1..3).each { |n| block.call n } }")
  .execute(polyglot.asValue((IntConsumer) n -> System.out.println(n)));
```

### Creating objects

JRuby embedding APIs don't have support for creating new objects, but you can
just call the `new` method yourself.

```java
((Invocable) scriptEngine).invokeMethod(scriptEngine.eval("Time"), "new", 2021, 3, 18);
container.callMethod(container.runScriptlet("Time"), "new", 2021, 3, 18)
ruby.evalScriptlet("Time").callMethod(ruby.getCurrentContext(), "new",
  new IRubyObject[]{ruby.newFixnum(2021), ruby.newFixnum(3), ruby.newFixnum(8)})
```

In TruffleRuby you can create an object from a Ruby `class` using `newInstance`.
You can use `canInstantiate` to see if this will be possible.

```java
polyglot.eval("ruby", "Time").newInstance(2021, 3, 18);
```

### Handling strings

In JRuby's embedding APIs you would use `toString` to convert to a Java
`String`.

In TruffleRuby you can use `isString` and `asString`.

### Accessing arrays

JRuby's arrays implement `List<Object>`, so you can cast to this interface to
access them.

```java
((List) scriptEngine.eval("[3, 4, 5]")).get(1);
((List) container.runScriptlet("[3, 4, 5]")).get(1);
((List) bsfManager.eval("jruby", "<script>", 1, 0, "[3, 4, 5]")).get(1);
((List) ruby.evalScriptlet("[3, 4, 5]")).get(1);
```

In TruffleRuby you can use `getArrayElement`, `setArrayElement`, and
`getArraySize`, or you can use `as(List.class)` to get a `List<Object>`.

```java
polyglot.eval("ruby", "[3, 4, 5]").getArrayElement(1);
polyglot.eval("ruby", "[3, 4, 5]").as(List.class).get(1);
```

### Accessing hashes

JRuby's hashes implement `Map<Object, Object>`, so you can cast to this
interface to access them.

```java
((Map) scriptEngine.eval("{'a' => 3, 'b' => 4, 'c' => 5}")).get("b");
((Map) scriptEngine.eval("{3 => 'a', 4 => 'b', 5 => 'c'}")).get(4);
```

In TruffleRuby you can use `getMember` and `putMember` if the keys are strings,
or you can use `as(Map.class)` to get a `Map<Object, Object>`.

```java
polyglot.eval("ruby", "{'a' => 3, 'b' => 4, 'c' => 5}").getMember("b");
polyglot.eval("ruby", "{'a' => 3, 'b' => 4, 'c' => 5}").as(Map.class).get("b");
```

If the keys aren't strings then there is no way to read that `Hash` from Java at
the moment.

### Implementing interfaces

You may want to implement a Java interface using a Ruby object.  (Example copied
from the JRuby wiki.)

```java
interface FluidForce {
  double getFluidForce(double a, double b, double depth);
}
```

```ruby
class EthylAlcoholFluidForce
  def getFluidForce(x, y, depth)
    area = Math::PI * x * y
    49.4 * area * depth
  end
end

EthylAlcoholFluidForce.new
```

In JSR 223 you can use `getInterface(object, Interface.class)`, in JRuby Embed
you can use `getInstance(object, Interface.class)`, and in direct embedding
you can use `toJava(Interface.class)`. BSF does not appear to support
implementing interfaces.

```java
FluidForce fluidForce = ((Invocable) scriptEngine).getInterface(scriptEngine.eval(RUBY_SOURCE), FluidForce.class);       
FluidForce fluidForce = container.getInstance(container.runScriptlet(RUBY_SOURCE), FluidForce.class);
FluidForce fluidForce = ruby.evalScriptlet(RUBY_SOURCE).toJava(FluidForce.class);
fluidForce.getFluidForce(2.0, 3.0, 6.0);
```

In TruffleRuby you can get an interface implemented by your Ruby object
by using `as(Interface.class)`.

```java
FluidForce fluidForce = polyglot.eval("ruby", RUBY_SOURCE).as(FluidForce.class);
fluidForce.getFluidForce(2.0, 3.0, 6.0);
```

JRuby allows the name of the Ruby method to be `get_fluid_force`, using Ruby
conventions, instead of `getFluidForce`, using Java conventions. TruffleRuby
does not support this at the moment.

### Implementing lambdas

As far as I know, JSR 223, BSF, JRuby Embed and direct embedding do not have a
convenient way to get a Java lambda from a Ruby lambda.

In TruffleRuby you can get a Java lambda (really an implementation of a
functional interface) from a Ruby lambda by using
`as(FunctionalInterface.class)`.

```java
BiFunction<Integer, Integer, Integer> adder = polyglot.eval("ruby", "lambda { |a, b| a + b }").as(BiFunction.class);
adder.apply(14, 2);
```

### Parse once run many times

Some of the JRuby embedding APIs allow a script to be compiled once and then
eval'd several times.

```java
CompiledScript compiled = ((Compilable) scriptEngine).compile("puts 'hello'");
compiled.eval();
```

In TruffleRuby you can simply return a lambda from parsing and execute this
many times. It will be subject to optimisation like any other Ruby code.

```java
Value parsedOnce = polyglot.eval("ruby", "lambda { run many times }");
parsedOnce.execute();
```

## Using Java from Ruby

TruffleRuby provides its own scheme for Java interop that is consistent for
use from any GraalVM language, to any other GraalVM language. This isn't
compatible with existing JRuby Java interop, so you will need to migrate.

Polyglot programming in general is [documented elsewhere](polyglot.md) - this
section describes it relative to JRuby.

This example is from the JRuby wiki:

```ruby
# This is the 'magical Java require line'.
require 'java'

# With the 'require' above, we can now refer to things that are part of the
# standard Java platform via their full paths.
frame = javax.swing.JFrame.new("Window") # Creating a Java JFrame
label = javax.swing.JLabel.new("Hello")

# We can transparently call Java methods on Java objects, just as if they were defined in Ruby.
frame.add(label)  # Invoking the Java method 'add'.
frame.setDefaultCloseOperation(javax.swing.JFrame::EXIT_ON_CLOSE)
frame.pack
frame.setVisible(true)
```

In TruffleRuby we would write that this way instead:

```ruby
Java.import 'javax.swing.JFrame'
Java.import 'javax.swing.JLabel'

frame = JFrame.new("Window")
label = JLabel.new("Hello")

frame.add(label)
frame.setDefaultCloseOperation(JFrame['EXIT_ON_CLOSE'])
frame.pack
frame.setVisible(true)
```

Instead of using Ruby metaprogramming to simulate a Java package name, we
explicitly import classes. Constants are read by reading properties of the
class rather than using Ruby notation.

### Require Java

There is no need to `require 'java'` in TruffleRuby. However, you do need to run
in `--jvm` mode. This is only available in GraalVM - not in the standalone
distribution installed by Ruby version managers and installers. You can feature
sniff `defined?(Java)`.

### Referring to classes

In JRuby Java classes can either be referenced in the `Java` module, such as
`Java::ComFoo::Bar`, or if they have a common TLD they can be referenced as
`com.foo.Bar`. `java_import com.foo.Bar` will define `Bar` as a top-level
constant.

In TruffleRuby Java classes are referred to using either
`Java.type('com.foo.Bar')`, which you would then normally assign to a constant,
or you can use `Java.import 'com.foo.Bar'` to have `Bar` defined as a top-level
constant.

### Wildcard package imports

JRuby lets you `include_package 'com.foo'` which will make all classes in that
package available as constants in the current scope.

In TruffleRuby you refer to classes explicitly.

### Calling methods and creating instances

In both JRuby and TruffleRuby you call Java methods as you would a Ruby method.

JRuby will rewrite method names such as `my_method` to the Java convention of
`myMethod`, and converts `getFoo` to `foo`, and `setFoo` to `foo=`. TruffleRuby
does not do these conversions.

### Referring to constants

In JRuby, Java constants are modelled as Ruby constants, `MyClass::FOO`. In
TruffleRuby you use the read notation to read them as a property,
`MyClass['FOO']`.

### Using classes from jar files

In JRuby you can add classes and jars to the classpath using `require`. In
TruffleRuby you use the `-classpath` JVM flag as normal.

### Additional Java-specific methods

JRuby defines these methods on Java objects.

`java_class` - use `class`.

`java_kind_of?` - use `is_a?`

`java_object` - not supported.

`java_send` - use `__send__`.

`java_method` - not supported.

`java_alias` - not supported.

### Creating Java arrays

In JRuby you use `Java::byte[1024].new`.

In TruffleRuby you would use `Java.type('byte[]').new(1024)`

### Implementing Java interfaces

JRuby there are several ways to implement an interface. For example to add
an action listener to a Swing button we could do any of these three things.

```ruby
class ClickAction
 include java.awt.event.ActionListener
 
 def actionPerformed(event)
   javax.swing.JOptionPane.showMessageDialog nil, 'hello'
 end
end

button.addActionListener ClickAction.new
```

```ruby
button.addActionListener do |event|
  javax.swing.JOptionPane.showMessageDialog nil, 'hello'
end
```

```ruby
button.addActionListener ->(event) {
  javax.swing.JOptionPane.showMessageDialog nil, 'hello'
}
```

In TruffleRuby we'd always use the last option to generate an interface.

```ruby
button.addActionListener ->(event) {
  JOptionPane.showMessageDialog nil, 'hello'
}
```

### Generating Java classes at runtime

JRuby supports converting a Ruby class to a concrete Java class using `become_java!`.

TruffleRuby does not support this. We recommend using a proper Java interface as
your interface between Java and Ruby.

### Reopening Java classes

Java classes cannot be re-opened in TruffleRuby.

### Subclassing Java classes

Java classes cannot be subclassed in TruffleRuby. Use interfaces instead.

### JRuby-compatible Java interop

TruffleRuby experimentally supports some of JRuby's approach to Java interop.
`require 'java'` to use it.

## Extending TruffleRuby using Java

JRuby supports extensions written in Java. These extensions are written against
an informal interface that is simply the entire internals of JRuby, similar to
how the MRI C extension interface works.

TruffleRuby does not support these extensions at the moment.

## Tooling

### Standalone classes and jars

JRuby supports compiling to standalone source classes and compiled jars
from Ruby using `jrubyc`.

TruffleRuby does not support compiling Ruby code to Java. We recommend using the
Polyglot API as your entry point from Java to Ruby.

### Warbler

JRuby supports building war files for loading into enterprise Java web servers.

TruffleRuby does not support this at the moment.

### VisualVM

VisualVM works for TruffleRuby as for JRuby.

Additionally, the VisualVM included in GraalVM understands Ruby objects, rather
than Java objects, when you use the heap dump tool.
