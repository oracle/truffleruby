# How-To Guide

-   [How to find a Core Method
    implementation](#TruffleRubyhowtoguide-HowtofindaCoreMethodimplementation)
-   [How to add a Core Method in
    Java](#TruffleRubyhowtoguide-HowtoaddaCoreMethodinJava)
-   [How to add a new C API
    function](#TruffleRubyhowtoguide-HowtoaddanewCAPIfunction)
-   [How to define and expose a POSIX system call to Ruby
    code](#TruffleRubyhowtoguide-HowtodefineandexposeaPOSIXsystemcalltoRubycode)
-   [How to add a helper Ruby
    method](#TruffleRubyhowtoguide-HowtoaddahelperRubymethod)
-   [How to warn in Ruby](#TruffleRubyhowtoguide-HowtowarninRuby)
-   [How to warn in Java](#TruffleRubyhowtoguide-HowtowarninJava)
-   [How to raise Ruby exception in
    Java](#TruffleRubyhowtoguide-HowtoraiseRubyexceptioninJava)
-   [How to cast type implicitly in
    Ruby](#TruffleRubyhowtoguide-HowtocasttypeimplicitlyinRuby)
-   [How to cast type implicitly in
    Java](#TruffleRubyhowtoguide-HowtocasttypeimplicitlyinJava)
-   [How to call Java code in
    Ruby](#TruffleRubyhowtoguide-HowtocallJavacodeinRuby)
-   [How to call Ruby method in
    Java](#TruffleRubyhowtoguide-HowtocallRubymethodinJava)
-   [How to declare an optional method argument in
    Ruby](#TruffleRubyhowtoguide-HowtodeclareanoptionalmethodargumentinRuby)
-   [How to accept keyword arguments in Core Method in
    Java](#TruffleRubyhowtoguide-HowtoacceptkeywordargumentsinCoreMethodinJava)
-   [How to create Ruby Array in
    Java](#TruffleRubyhowtoguide-HowtocreateRubyArrayinJava)
-   [How to call original class method in
    Ruby](#TruffleRubyhowtoguide-HowtocalloriginalclassmethodinRuby)
-   [How to call original instance method in
    Ruby](#TruffleRubyhowtoguide-HowtocalloriginalinstancemethodinRuby)
-   [How to get a list of all the
    Primitives](#TruffleRubyhowtoguide-HowtogetalistofallthePrimitives)
-   [How to use the most common
    Primitives](#TruffleRubyhowtoguide-HowtousethemostcommonPrimitives)
-   [How to use Java debug
    helpers](#TruffleRubyhowtoguide-HowtouseJavadebughelpers)
-   [How to update supported Unicode
    version](#TruffleRubyhowtoguide-HowtoupdatesupportedUnicodeversion)
-   [How to patch MRI source files when update
    Ruby](#TruffleRubyhowtoguide-HowtopatchMRIsourcefileswhenupdateRuby)
-   [How to introduce a new platform-specific constant in
    Ruby](#TruffleRubyhowtoguide-Howtointroduceanewplatform-specificconstantinRuby)
-   [How to decide on what to change - ABI version or ABI
    check](#TruffleRubyhowtoguide-Howtodecideonwhattochange-ABIversionorABIcheck)
-   [How to choose where to add new specs - in TruffleRuby or in
    RubySpec
    repository](#TruffleRubyhowtoguide-Howtochoosewheretoaddnewspecs-inTruffleRubyorinRubySpecrepository)
-   [How to choose between Ruby and Java when implement a Ruby Core
    Library class
    method](#TruffleRubyhowtoguide-HowtochoosebetweenRubyandJavawhenimplementaRubyCoreLibraryclassmethod)
-   [How to write specs for C
    API](#TruffleRubyhowtoguide-HowtowritespecsforCAPI)
-   [How to exclude/include a RubySpec/MRI test
    case](#TruffleRubyhowtoguide-Howtoexclude/includeaRubySpec/MRItestcase)
-   [How to tag slow RubySpec
    tests](#TruffleRubyhowtoguide-HowtotagslowRubySpectests)
-   [How to introduce a constant  in
    specs](#TruffleRubyhowtoguide-Howtointroduceaconstantinspecs)
-   [How to add a new spec](#TruffleRubyhowtoguide-Howtoaddanewspec)

## How to find a Core Method implementation

Core Library is implemented partially in Ruby and partially in Java.

You can find Core Library classes implemented in Ruby in the
`src/main/ruby/truffleruby/core` directory, e.g.

-   `array.rb`
-   `file.rb`
-   ...

Java implementation is located in the
`src/main/java/org/truffleruby/core` directory in
`<Ruby-class-name>Nodes.java` files.

Such a file contains a class declaration like the following one:

```java
@CoreModule(value = "FalseClass", isClass = true)
public abstract class FalseClassNodes {
    // ...
}
```

where `value = "FalseClass"` means a Ruby class (`FalseClass` in
our case which methods are implemented in this file).

Such a class contains implementation of Ruby class methods that are
declared in the following way:

```java
@CoreMethod(names = "&", needsSelf = false, required = 1)
public abstract static class AndNode extends CoreMethodArrayArgumentsNode {
    // ...
}
```

where `names = "&"` means a Ruby method name (`FalseClass#&` in
our case).

## How to add a Core Method in Java

Ruby Core Methods (methods of Ruby classes from the Core Library)
implemented in Java are represented as AST (Abstract Syntax Tree) nodes
that have an entry point - `execute` method.

Core Methods are located in `src/main/java/org/truffleruby/core`
directory. File names follow the same pattern: `<RubyClass>Nodes`, e.g.
`ArrayNodes`, `HashNodes`, `KernelNodes` etc. Each Core Method is
represented by a Java class named after it, e.g.
`StringNodes.SizeNode` implements Ruby `String#size` method.

Let's consider and example - implementation of `Hash#clear` Core
Method:

```java
// HashNodes.java
@CoreMethod(names = "clear", raiseIfFrozenSelf = true)
@ImportStatic(HashGuards.class)
public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

    @Specialization(limit = "hashStrategyLimit()")
    protected RubyHash clear(RubyHash hash,
            @CachedLibrary("hash.store") HashStoreLibrary hashes) {
        hashes.clear(hash.store, hash);
        return hash;
    }
}
```

Let's read this example line by line.

```java
@CoreMethod(names = "clear", raiseIfFrozenSelf = true)
```

The first annotation `@CoreMethod` declares some Core Method
properties, e.g. Ruby method name (`clear`), parameters number etc.
`raiseIfFrozenSelf` attribute here means that it's a method that
mutates a Hash instance, so it shouldn't be called on a frozen Hash.
The `FrozenError` will be raised in this case.

```java
@ImportStatic(HashGuards.class)
```

`@ImportStatic` annotation makes available in the `ClearNode` class
some helper methods (guards in this case) declared in the `HashGuards`
class.

```java
public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {
```

A Core Method class is called after a Ruby method with a suffix
`Node`. It usually inherits `CoreMethodArrayArgumentsNode` class but
there are other options that could be met in the wild:

-   CoreMethodNode
-   RubyContextSourceNode
-   AlwaysInlinedMethodNode

```java
@Specialization(limit = "hashStrategyLimit()")
```

`@Specialization` annotation declares a Java method (`clear` in our
case) as a partial implementation. There are usually several such
specializations in a Core Method class - and what method-specialization
to choose depends on actual argument types.

```java
protected RubyHash clear(RubyHash hash,
```

Core Method specialization method (that implements a non-singleton Ruby
method) accepts a receiver object (`self`) as first parameter. All the
subsequent parameters are parameters of a Ruby method itself. In this
case the method `clear` doesn't have parameters at all. Ruby Core
Library classes are represented in Java as Java classes, e.g.
`RubyHash`, `RubyArray`, `RubyString`, `RubySymbol` etc.

```
@CachedLibrary("hash.store") HashStoreLibrary hashes) {
```

It's a declaration of auxiliary parameter that will be passed
automatically by the runtime. `HashStoreLibrary` is a helper class to
manipulate internal representation (`hash.store`) of Ruby Hash to
store key-value pairs.

```java
hashes.clear(hash.store, hash);
```

It's the main line in the whole class - clearing of a Hash. Actually,
the logic is implemented in the `HashStoreLibrary` so it's just asked
to perform the operation with proper arguments.

```java
return hash;
```

Method `Hash#clear` returns `self`, so the method returns `hash`
argument - receiver of the `clear` Ruby method.

### Specializations

The idea behind specializations is to have different (optimized)
implementations for different arguments.

Let's consider one more example - implementation on `Integer#<`:

```java
// IntegerNodes.java
@CoreMethod(names = "<", required = 1)
public abstract static class LessNode extends CoreMethodArrayArgumentsNode {

    @Specialization
    protected boolean less(int a, int b) {
        return a < b;
    }

    @Specialization
    protected boolean less(long a, long b) {
        return a < b;
    }

    @Specialization
    protected boolean less(long a, double b) {
        return a < b;
    }

    @Specialization
    protected boolean less(long a, RubyBignum b) {
        return BigIntegerOps.isPositive(b); // Bignums are never long-valued.
    }

    @Specialization
    protected boolean less(RubyBignum a, long b) {
        return BigIntegerOps.isNegative(a); // Bignums are never long-valued.
    }

    @Specialization
    protected boolean less(RubyBignum a, double b) {
        return BigIntegerOps.less(a, b);
    }

    @Specialization
    protected boolean less(RubyBignum a, RubyBignum b) {
        return BigIntegerOps.compare(a, b) < 0;
    }

    @Specialization(guards = "!isRubyNumber(b)")
    protected Object lessCoerced(Object a, Object b,
            @Cached DispatchNode redoCompare) {
        return redoCompare.call(a, "redo_compare", coreSymbols().LESS_THAN, b);
    }
}
```

There are several specializations defined which have different argument
types and proper comparison logic.

There are two main scenarios - when both objects are Numeric and when
the second argument (`b`, `a` is self, we remember this) is not
Numeric and it should be coerced. All specializations except the last
one cover the first case and the last specialization tries to coerce by
calling a helper method (`redo_compare`, that is implemented in Ruby).

There are the following cases for Numeric arguments:

- int, int
- long, long
- long, double
- long, RubyBignum
- RubyBignum, long
- RubyBignum, double
- RubyBignum, RubyBignum

So for `1 < 2` Ruby expression the first specialization (int, int)
will be used. When one of the numbers doesn't fit into Java int, but
that fits into Java long - then the second specialization (long, long)
will be used, and so on.

### Guards

Let's look at the last specialization in the previous example:

```java
@Specialization(guards = "!isRubyNumber(b)")
protected Object lessCoerced(Object a, Object b,
        @Cached DispatchNode redoCompare) {
    return redoCompare.call(a, "redo_compare", coreSymbols().LESS_THAN, b);
}
```

The first line is pretty interesting - there is a `guards` attribute
that contains something like a code expression as a String literal:

```java
@Specialization(guards = "!isRubyNumber(b)")
```

The `guards` attribute contains a Java boolean expression and is used
to express some additional (to parameter types) and more complex
conditions to choose a specialization. It can contain Java method calls
that should be declared in the class as public methods or imported (with
`@ImportStatic`) from some helper class (e.g. `StringGuards` or
`HashGuards`). The most common guard predicates are defined in
`RubyGuards` and by default available in any Core Method class.

Examples of some guards:

- `"count < 0"`
- `"array != other"`
- `"wasProvided(name)"`
- `"range.isEndless() || range.isBoundless()"`

May be specified several expressions that are combined with logical AND:

```java
@Specialization(guards = { "!isEmptyArray(array)", "count >= 0", "!fitsInInteger(count)" })
```

### Fallback

There is a way to declare a special specialization that is called when
all the other ones in a class don't match arguments. It's declared
with a `@Fallback` annotation this way:

```java
@Fallback
protected boolean immutable(Object object, Object name) {
    return false;
}
```

Note that parameters are declared as instances of `Object` type.

Let's consider an example of `Kernel#instance_variable_defined?`
implementation:

```java
// KernelNodes.java
@CoreMethod(names = "instance_variable_defined?", required = 1)
public abstract static class InstanceVariableDefinedNode extends CoreMethodArrayArgumentsNode {

    @Specialization
    protected boolean isInstanceVariableDefined(RubyDynamicObject object, Object name,
            @Cached CheckIVarNameNode checkIVarNameNode,
            @CachedLibrary(limit = "getDynamicObjectCacheLimit()") DynamicObjectLibrary objectLibrary,
            @Cached NameToJavaStringNode nameToJavaStringNode) {
        final String nameString = nameToJavaStringNode.execute(name);
        checkIVarNameNode.execute(object, nameString, name);
        return objectLibrary.containsKey(object, nameString);
    }

    @Fallback
    protected boolean immutable(Object object, Object name) {
        return false;
    }
}
```

The logic is the following

- when self (the first parameter) is `RubyDynamicObject` (so it
  isn't a Symbol or Numeric) then it can have instance variables
- otherwise it cannot have instance variable at all

The method `immutable` is called only when the first argument isn't
`RubyDynamicObject`.

### Method with variable arguments

There are a lot of methods in the Ruby Core Library that accept unknown
number of arguments, e.g. #send, Module#class_eval, etc. There is a way
to implement such a method in Java - just specify `rest = true`
`@CoreMethod` annotation attribute and specializations will have a bit
different parameters list - with `Object[] rubyArgs` argument.

Let's consider an example of `String#concat` method. The most generic
specialization is the following:

```java
// ModuleNodes.java
@CoreMethod(names = "private_class_method", rest = true)
public abstract static class PrivateClassMethodNode extends CoreMethodArrayArgumentsNode {
    // ...

    @Specialization
    protected RubyModule privateClassMethod(VirtualFrame frame, RubyModule module, Object[] names) {
        final RubyClass singletonClass = singletonClassNode.executeSingletonClass(module);

        for (Object name : names) {
            setMethodVisibilityNode.execute(singletonClass, name, Visibility.PRIVATE);
        }

        return module;
    }
}
```

Let's look at the method parameters list:

```java
protected RubyModule privateClassMethod(VirtualFrame frame, RubyModule module, Object[] names) {
```

The first parameter is an optional argument `frame`. The second one
(`RubyModule module`) is a receiver, that's `self`. `Object[]
names` are actual method call arguments.

### Block argument

If a Core Method accepts a block argument, it should declare the last
parameter as `RubyProc block`.

Let's consider an example - implementation of `Module#initialize`
method:

```java
// ModuleNodes.java
@CoreMethod(names = "initialize", needsBlock = true)
public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

    public abstract RubyModule executeInitialize(RubyModule module, Object block);

    @Specialization
    protected RubyModule initialize(RubyModule module, Nil block) {
        return module;
    }

    @Specialization
    protected RubyModule initialize(RubyModule module, RubyProc block,
            @Cached ClassExecBlockNode classExecBlockNode) {
        classExecBlockNode.execute(EmptyArgumentsDescriptor.INSTANCE, module, new Object[]{ module }, block);
        return module;
    }
}
```

A specialization signature looks like:

```java
protected RubyModule initialize(RubyModule module, RubyProc block,
```

It uses the `ModuleNodes.ClassExecBlockNode` helper node to call a
block that uses under the hood `CallBlockNode` node. `CallBlockNode`
is a common way to call a Ruby block.

### Optional parameter

A lot of Ruby Core Methods have multiple signatures with different
numbers of arguments. So it's a common situation when a positional
parameter might be missing. In Java a Core Method specialization missing
an optional parameter is expressed with `NotProvided` class. There are
several similar approaches to handle optional parameters.

#### Explicit check

The approach is the following:

- declare `Object object` parameter
- manually check if it equals `NotProvided.INSTANCE`

```java
protected RubyArray unpackCached(Object string, Object format, Object offsetObject,
    // ...
{

    final int offset = (offsetObject == NotProvided.INSTANCE) ? 0 : (int) offsetObject;
```

#### Check with guard

There are dedicated guards (declared in `RubyGuards`):

- wasProvided
- wasNotProvided

```java
protected Object stepFallback(VirtualFrame frame, Object range, Object step, Object block) {
    // ...

    if (RubyGuards.wasNotProvided(step)) {
        step = 1;
    }
```

#### Check on the specializations level

When cases when parameter is present and missing are handled in
different specializations you can rely on filtering by specialization
signature and:

-   define a specialization with `NotProvided object`
-   define another specialization with some specific type, e.g. `int
    object`

Specifying different combinations of parameter types you can easily
differentiate use cases. Let's consider an example:

```java
// HashNodes.java
@CoreMethod(names = "initialize", needsBlock = true, optional = 1, raiseIfFrozenSelf = true,
        split = Split.HEURISTIC)
@ImportStatic(HashGuards.class)
public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

    @Specialization
    protected RubyHash initialize(RubyHash hash, NotProvided defaultValue, Nil block) {
        // ...
    }

    @Specialization
    protected RubyHash initialize(RubyHash hash, NotProvided defaultValue, RubyProc block,
            @Shared @Cached PropagateSharingNode propagateSharingNode) {
        // ...
    }

    @Specialization(guards = "wasProvided(defaultValue)")
    protected RubyHash initialize(RubyHash hash, Object defaultValue, Nil block,
            @Shared @Cached PropagateSharingNode propagateSharingNode) {
        // ...
    }

    @Specialization(guards = "wasProvided(defaultValue)")
    protected Object initialize(RubyHash hash, Object defaultValue, RubyProc block) {
        throw new RaiseException(
                getContext(),
                coreExceptions().argumentError("wrong number of arguments (1 for 0)", this));
    }
```

Please note that in the 3d specialization `wasProvided(defaultValue)`
guard is used. The 1st and the 3d specializations have similar
signatures and match the case when `defaultValue` argument is missing
and an instance of `NotProvided` is passed (because `Object` class
is more generic than `NotProvided`). So the guard is the only way to
differentiate them.

### Class methods

When you need to implement in Java a class method, not instance one -
use `onSingleton = true` attribute of the `@CoreMethod` annotation:

```java
// HashNodes.java
@CoreMethod(names = "ruby2_keywords_hash?", onSingleton = true, required = 1)
public abstract static class IsRuby2KeywordsHashNode extends CoreMethodArrayArgumentsNode {
    @Specialization
    protected boolean isRuby2KeywordsHash(RubyHash hash) {
        return hash.ruby2_keywords;
    }
}
```

Please note that a specialization's arguments don't contain `self` -
only a Ruby method's own parameters (`RubyHash hash` in our example).

### Declare argument type casting

There are helper nodes for implicit type casting e.g. `ToIntNode`,
`ToAryNode` or `ToStrNode`. There is a way to declare type casting
of Core Method arguments - with `@CreateCast` annotation.

Let's consider an example of `Module#alias_method` method
implementation:

```java
// ModuleNodes.java
@CoreMethod(names = "alias_method", required = 2, raiseIfFrozenSelf = true, split = Split.NEVER)
@NodeChild(value = "module", type = RubyNode.class)
@NodeChild(value = "newName", type = RubyBaseNodeWithExecute.class)
@NodeChild(value = "oldName", type = RubyBaseNodeWithExecute.class)
public abstract static class AliasMethodNode extends CoreMethodNode {

    @CreateCast("newName")
    protected RubyBaseNodeWithExecute coerceNewNameToSymbol(RubyBaseNodeWithExecute newName) {
        return ToSymbolNode.create(newName);
    }

    @CreateCast("oldName")
    protected RubyBaseNodeWithExecute coerceOldNameToSymbol(RubyBaseNodeWithExecute oldName) {
        return ToSymbolNode.create(oldName);
    }

    @Specialization
    protected RubySymbol aliasMethod(RubyModule module, RubySymbol newName, RubySymbol oldName) {
        return aliasMethod(module, newName, oldName, this);
    }
```

Actual type casting to Symbol (with a `ToSymbolNode` class) is moved
out from a specialization to helper methods `coerceNewNameToSymbol`
and `coerceOldNameToSymbol`.

```java
@CreateCast("newName")
protected RubyBaseNodeWithExecute coerceNewNameToSymbol(RubyBaseNodeWithExecute newName) {
    return ToSymbolNode.create(newName);
}
```

Please note that the helper methods don't perform casting itself. They
build and return an AST node that will be executed and cast arguments to
Symbols before calling a proper specialization.

The specialization accepts already caster RubySymbol values:

```java
protected RubySymbol aliasMethod(RubyModule module, RubySymbol newName, RubySymbol oldName) {
```

## How to add a new C API function

Ruby provides the C API so TruffleRuby should comply with this API as
well.

C API functions implementation is located in the `src/main/c/cext`
directory.

C function can just proxy a call to implementation in Ruby, e.g.
function `rb_time_nano_new` calls (using `polyglot_invoke` function)
another function with the same name and passes all the parameters:

```c
// src/main/c/cext/time.c
VALUE rb_time_nano_new(time_t sec, long nsec) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_time_nano_new", sec, nsec));
}
```

Ruby methods available for C API functions are located in
`lib/truffle/truffle/cext.rb` file and defined in `Truffle::CExt`
module.

```ruby
  # lib/truffle/truffle/cext.rb
  ORIGINAL_TIME_AT = Time.method(:at)

  def rb_time_nano_new(sec, nsec)
    ORIGINAL_TIME_AT.call(sec, nsec, :nanosecond)
  end
```

C extensions related *primitives* are located in
`org/truffleruby/cext/CExtNodes.java` file.

More details about writing a C extensions are provided in the C
Extensions Guide
([https://github.com/oracle/truffleruby/blob/master/doc/contributor/cexts.md](https://github.com/oracle/truffleruby/blob/master/doc/contributor/cexts.md){.external-link
rel="nofollow"}).

## How to define and expose a POSIX system call to Ruby code

There is a way to make a POSIX system call in Ruby code

Use a `Truffle::POSIX` Ruby module to call available syscalls:

```ruby
# src/main/ruby/truffleruby/core/dir.rb
ret = Truffle::POSIX.chdir path
```

All the available system calls are listed in
`src/main/ruby/truffleruby/core/posix.rb` file in the following way:

```ruby
# src/main/ruby/truffleruby/core/posix.rb  
attach_function :chdir, [:string], :int
```

### TruffleRuby specific wrappers

There are also helper C functions and wrappers around POSIX system calls
that are also available in the `Truffle::POSIX` module.

They are declared in the following way:

```ruby
# src/main/ruby/truffleruby/core/posix.rb
attach_function :truffleposix_clock_gettime, [:int], :int64_t, LIBTRUFFLEPOSIX
```

And implemented in the `src/main/c/truffleposix/truffleposix.c` file,
e.g. this function returns current time in nanoseconds:

```c
// src/main/c/truffleposix/truffleposix.c
int64_t truffleposix_clock_gettime(int clock) {
  struct timespec timespec;
  int ret = clock_gettime((clockid_t) clock, ×pec);
  if (ret != 0) {
    return 0;
  }
  return ((int64_t) timespec.tv_sec * 1000000000) + (int64_t) timespec.tv_nsec;
}
```

### Other C libraries

There are also available functions to expose from *libc* and *libcrypt*
as well.

## How to add a helper Ruby method

You would like to extract some code from a Ruby Core class (`Array`,
`IO`, etc) method but don't want it to be visible to the end user. So
a new helper class method or a private instance method is not an option.

The common solution is to create a new Operations helper module:

- create a new Ruby file
  `src/main/ruby/truffleruby/core/truffle/<core-class>_operations.rb`
- create a new Ruby module `Truffle::<core-class>Operations`
- add this file into a `CORE_FILES` list in
  `src/main/java/org/truffleruby/core/CoreLibrary.java`
- extract helper methods to this module `<core-class>Operations`

## How to warn in Ruby

Use `Kernel#warn` method.

```ruby
warn 'not acting on top...'
warn 'IO::new() does not...', uplevel: 1
warn "`byte_ranges` is deprecated..." if $VERBOSE
```

Use `uplevel: 1` option to print a file and a line number of an
application code in a warning message if a warning is triggered in Core
Library Ruby source code.

## How to warn in Java

There are two helper nodes

Use `WarnNode` to warn unconditionally:

```ruby
@Specialization
protected Object powBignum(long base, RubyBignum exponent,
        @Cached("new()") WarnNode warnNode) {
    // ...

    if (warnNode.shouldWarn()) {
        warnNode.warningMessage(
                getContext().getCallStack().getTopMostUserSourceSection(),
                "in a**b, b may be too big");
    }
    //... 
}
```

Use `WarningNode` to warn only if `$VERBOSE` is true:

```java
@Specialization
protected Object taint(Object object,
        @Cached("new()") WarningNode warningNode) {
    if (warningNode.shouldWarn()) {
        warningNode.warningMessage(
                getSourceSection(),
                "Object#taint is deprecated and will be removed in Ruby 3.2.");
    }
    return object;
}
```

Use `getContext().getCallStack().getTopMostUserSourceSection()`
instead of `getSourceSection()` to print a file and a line number of
an application code in warning message if warning is triggered in Core
Library Ruby source code.

## How to raise Ruby exception in Java

Use `throw new RaiseException(...)` to raise a specified Ruby
exception

```java
throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
```

Use helper methods (e.g. `typeError` or `frozenError`) to construct
a Ruby exception object defined in a `CoreExceptions` class.

Use a helper method `CoreExceptions.inspect` to add a string
representation of arbitrary Ruby object to an exception message

```java
public RubyException frozenError(Object object, Node currentNode) {
    String className = LogicalClassNode.getUncached().execute(object).fields.getName();
    return frozenError(StringUtils.format("can't modify frozen %s: %s", className, inspect(object)), currentNode,
            object);
}
```

## How to cast type implicitly in Ruby

Ruby Core library uses implicit type conversion to convert method
arguments to expected types using methods like `#to_int`, `#to_str`,
`#to_hash`, `#to_io` etc.

To convert arguments to an expected type in Ruby there are two types of
helpers:

- Ruby methods defined in a `Truffle::Type` module
- *primitives* defined in
  `src/main/java/org/truffleruby/core/support/TypeNodes.java`

Main helper methods for implicit type conversion are:

- `Truffle::Type.rb_convert_type`
- `Truffle::Type.rb_check_convert_type`

Example for `rb_convert_type`

```ruby
pid = Truffle::Type.rb_convert_type(pid, Integer, :to_int)
```

The main difference between `rb_convert_type` and
`rb_check_convert_type` is that the last one doesn't raise exception
if object doesn't respond to specified method (e.g. `#to_str`) and
returns `nil` instead. It's useful if there are several expected
types or several ways to convert object (e.g. with both `#to_int` and
`#to_i` methods).

Example for `rb_check_convert_type`:

```ruby
# core/kernel.rb
def String(obj)
  str = Truffle::Type.rb_check_convert_type(obj, String, :to_str)
  if Primitive.nil? str
    str = Truffle::Type.rb_convert_type(obj, String, :to_s)
  end
  str
end
```

There are more specific *primitives* defined in `TypeNodes.java`.

The most common are the following:
- `Primitive.rb_num2long`
- `Primitive.rb_num2int`
- `Primitive.rb_to_int`
- `Primitive.as_boolean`

## How to cast type implicitly in Java

Ruby Core library uses implicit type conversion to convert method
arguments to expected types using methods like `#to_int`, `#to_str`,
`#to_hash`, `#to_io` etc.

To convert arguments to an expected type in Java there are helper
classes in a `org.truffleruby.core.cast` package.

The most common helpers are:

- ToAryNode
- ToIntNode
- ToStrNode

Example:

```java
@Specialization(guards = "range.isEndless()")
protected int[] normalizeEndlessRange(RubyObjectRange range, int size,
        @Cached ToIntNode toInt) {
    int begin = toInt.execute(range.begin);
    return new int[]{ begin >= 0 ? begin : begin + size, size - begin };
}
```

## How to call Java code in Ruby

Use *primitives*, Java classes defined in a special way, to be able to
call them in Ruby source code. *Primitives* are usually located in the
`<RubyClass>Nodes` files among Core Method nodes.

Define a *primitive* with `@Primitive` Java annotation and a `name`
attribute:

```java
@Primitive(name = "nil?")
public abstract static class IsNilNode extends PrimitiveArrayArgumentsNode {
    @Specialization
    protected boolean isNil(Object value) {
        return value == nil;
    }
}
```

and use it in Ruby source code as `Primitive.<method name>`:

```ruby
def compact
  reject { |_k, v| Primitive.nil? v }
end
```

To use a *primitive* it may be required to add a magic comment at the
beginning of a file (*primitives* are available by default in Core
Library files):

```ruby
# truffleruby_primitives: true
```

Naming conventions are described in the Primitives Guide
<https://github.com/oracle/truffleruby/blob/master/doc/contributor/primitives.md>.

## How to call Ruby method in Java

Use `DispatchNode` class to call a method of arbitrary Ruby object:

```java
@Specialization(guards = { "!string.isFrozen()", "!compareByIdentity" })
protected Object dupAndFreeze(RubyString string, boolean compareByIdentity,
        @Cached DispatchNode dupNode) {
    final RubyString copy = (RubyString) dupNode.call(string, "dup");
    copy.freeze();
    return copy;
}
```

### With arguments

Ruby method arguments might be passed as positional arguments up to 3:

```java
callWarnNode.call(context.getCoreLibrary().kernelModule, "warn", warningString); // WarnNode
respondToMissingNode.call(self, "respond_to_missing?", toSymbolNode.execute(name), includeProtectedAndPrivate) // KernelNodes
toEnumWithSize.call( // EnumeratorSizeNode
    coreLibrary().truffleKernelOperationsModule,
    "to_enum_with_size",
    self,
    methodName,
    sizeMethodName);
```

If there are more than 3 arguments - they may be passes as an Array:

```java
// TruffleRegexpNodes
warnOnFallbackNode.call(
    getContext().getCoreLibrary().truffleRegexpOperationsModule,
    "warn_fallback",
    new Object[]{
        regexp,
        string,
        encoding,
        fromPos,
        toPos,
        atStart,
        startPos });
```

There are also useful helper methods:

- callWithKeywords
- callWithBlock

### Arguments manipulation

The methods listed above are just kind of helpers to simplify working
with DispatchNode.dispatch method that accepts arguments in a "frame
arguments" format. There are numerous helpers in a `RubyArguments`
class to create, change and query "frame arguments":

- pack
- repack
- set*
- get*

Example of repack-ing "frame arguments" where the all the
`Kernel#public_send` arguments except the first one (method name to call)
are passed to specified method call:

```java
// ClassNodes
@GenerateUncached
@CoreMethod(names = "public_send", needsBlock = true, required = 1, rest = true, alwaysInlined = true)
public abstract static class PublicSendNode extends AlwaysInlinedMethodNode {

    @Specialization
    protected Object send(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
            @Cached(parameters = "PUBLIC") DispatchNode dispatchNode,
            @Cached NameToJavaStringNode nameToJavaString) {
        Object name = RubyArguments.getArgument(rubyArgs, 0);
        Object[] newArgs = RubyArguments.repack(rubyArgs, self, 1);
        return dispatchNode.dispatch(callerFrame, self, nameToJavaString.execute(name), newArgs);
    }
}
```

## How to declare an optional method argument in Ruby

It's a common situation when a Ruby core class method has an optional
parameter.

Problem: you need to distinguish cases when argument is not passed and
when passed `nil` value.

Solution: you may use `undefined` as a default value and a
`Primitive.undefined?` primitive to check the value:

```ruby
# core/enumerable.rb
def min(n = undefined, &block)
  return min_n(n, &block) if !Primitive.undefined?(n) && !Primitive.nil?(n)
  min_max(-1, &block)
end
```

## How to accept keyword arguments in Core Method in Java

Currently there is no way to accept directly keyword arguments in Java
Core Method implementations.

There is a workaround:

1. to create a *primitive* in Java
2. to declare a Core Method in Ruby
3. to call the *primitive* and pass keyword arguments as positional
   arguments

```ruby
# core/string.rb
def unpack(format, offset: undefined)
  format = Truffle::Type.rb_convert_type(format, String, :to_str)
  unless Primitive.undefined?(offset)
    offset = Primitive.rb_num2int(offset) # to guarantee it's `int` finally
  end
  Primitive.string_unpack(self, format, offset)
end
```

## How to create Ruby Array in Java 

There is `createArray` helper method available in `RubyNode`
subclasses:

```java
createArray(newStore, size)
createArray(bytes);
```

## How to call original class method in Ruby

You want to implement a Ruby Core library class' method using another
Ruby Core class method and handle a case when this method is redefined
by a user code.

You may call original class method in the following way:

```ruby
ORIGINAL_TIME_AT = Time.method(:at)
ORIGINAL_TIME_AT.call(sec, nsec, :nanosecond)
```

## How to call original instance method in Ruby

You want to implement a Ruby Core library class' method using another
Ruby Core instance method and handle a case when this method is
redefined by a user code.

You may call original instance method in the following way:

```ruby
ARRAY_APPEND = Array.instance_method(:<<)
ARRAY_APPEND.bind_call(obj, *arguments)
```

## How to get a list of all the Primitives

Use `Truffle::Debug.primitive_names` method:

```
jt -q ruby -e 'puts Truffle::Debug.primitive_names.sort'
...
array_eql
array_equal
array_flatten_helper
array_inject
...
```

## How to use the most common Primitives

I would say the following *primitives* are the most often used and
generic:

- `Primitive.nil?`
- `Primitive.undefined?`
- `Primitive.check_frozen`
- `Primitive.single_block_arg`
- `Primitive.is_a?`
- `Primitive.respond_to?`
- `Primitive.class`
- `Primitive.equal?`

#### `Primitive.nil?(object)`

It's equivalent to `object == nil` or `object.nil?`

#### `Primitive.undefined?(object)`

It's equivalent to `object == undefined` where `undefined` is a
special magic unique object.

`undefined` object is used to detect whether an optional positional
argument is passed or not and to distinguish default "undefined" value
and `nil` that may be specified. `undefined` is an instance of
`NotProvided` Java class - `NotProvided.INSTANCE`.

```ruby
# core/string.rb
def initialize(other = undefined, capacity: nil, encoding: nil)
  unless Primitive.undefined?(other)
    Primitive.check_mutable_string self
    Primitive.string_initialize(self, other, encoding)
  end
  self.force_encoding(encoding) if encoding
  self
end
```

#### `Primitive.check_frozen(object)`

It's equivalent to `raise FrozenError if object.frozen?`.

It's used usually in self modifying methods to check whether self is
frozen (so it cannot be modified) and to raise `FrozenError` if it is.

```ruby
# core/array.rb
def sort!(&block)
  Primitive.check_frozen self

  Primitive.steal_array_storage(self, sort(&block))
end
```

#### `Primitive.single_block_arg`

It's used to get a block first argument.

It returns either:

- a first argument if there is a single argument passed
- an array of arguments if there are multiple arguments passed

```ruby
# core/enumerable.rb
def include?(obj)
  each { return true if Primitive.single_block_arg == obj }
  false
end
```

#### `Primitive.is_a?(object, <class>)`

It's equivalent to `object.is_a?(<class>)`.

#### `Primitive.respond_to?(object, name, include_private)`

It's equivalent to `object.respond_to?(name, include_private)`.

#### `Primitive.class(object)`

It's equivalent to `object.class`

#### `Primitive.equal?(object, object)`

It's equivalent to `a.equal?(b)`

## How to use Java debug helpers

The helper methods are implemented in Java in a
`src/main/java/org/truffleruby/debug/TruffleDebugNodes.java` file and
available in Ruby under a `Truffle::Debug` Ruby module.

There are the following Java debug helper methods:

- print
- tstring_to_debug_string
- flatten_string
- break_handle
- remove_handle
- java_class_of
- print_backtrace
- yarp_serialize
- yarp_parse
- yarp_execute
- parse_ast - JRuby parser specific
- ast
- print_ast
- print_source_sections
- ast_size
- shape
- array_storage
- array_capacity
- hash_storage
- shared?
- log_warning
- log_info
- log_config
- throw_java_exception
- throw_java_exception_with_cause
- throw_assertion_error
- assert
- Primitive.assert
- java_class
- java_object
- java_null
- java_character
- foreign_null
- foreign_pointer
- foreign_object
- foreign_object_with_members
- foreign_array
- foreign_pointer_array
- foreign_iterator
- foreign_iterable
- foreign_hash
- foreign_executable
- foreign_identity_function
- foreign_string
- foreign_exception
- foreign_boxed_value
- long
- associated
- drain_finalization_queue
- frame_declaration_context_to_string
- get_frame_bindings
- parse_name_of_method
- create_polyglot_thread
- primitive_names
- cexts_to_native_count

### Truffle::Debug.java_class_of

```
jt -q ruby -e 'puts Truffle::Debug.java_class_of([])'
RubyArray
```

### Truffle::Debug.yarp_serialize

```
jt -q ruby -e 'puts Truffle::Debug.yarp_serialize("1").dump'
"YARP\x00\x04\x00@7\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00L\f\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00S\x19\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x01\x00\x00\x00&\b\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x00"
```

### Truffle::Debug.yarp_parse

```
jt -q ruby -e 'puts Truffle::Debug.yarp_parse("[].to_ary")'
ProgramNode
  Scope
  StatementsNode
    CallNode
      ArrayNode
```

### Truffle::Debug.ast

```
jt -q ruby -e 'p Truffle::Debug.ast([].method(:to_ary))'
[:RubyMethodRootNode, [:SequenceNode, [:WriteLocalVariableNode, [:ProfileArgumentNodeGen, [:ReadSelfNode]]], [:SaveMethodBlockNode], [:SelfNode]]]
```

### Truffle::Debug.print_ast

```
jt -q ruby -e 'puts Truffle::Debug.print_ast([].method(:to_ary))'
RubyMethodRootNode
body = SequenceNode
body[0] = WriteLocalVariableNode
valueNode = ProfileArgumentNodeGen
childNode_ = ReadSelfNode
body[1] = SaveMethodBlockNode
body[2] = SelfNode
```

The method `Array#to_ary` is implemented in the following way:

```ruby
def to_ary
  self
end
```

### Truffle::Debug.ast_size

```
jt -q ruby -e 'puts Truffle::Debug.ast_size([].method(:to_ary))'
7
```

## How to update supported Unicode version

We rely on
[https://github.com/jruby/jcodings](https://github.com/jruby/jcodings){.external-link
rel="nofollow"} for Unicode/Encoding stuff. So basically updating
Unicode version is just as upgrade of this library

Steps to do:

- choose a proper jcodings version (probably the latest one)
  - Unicode version is hardcoded here
    <https://github.com/jruby/jcodings/blob/jcodings-1.0.58/src/org/jcodings/Config.java>
- ask for approval to upgrade the third-party component (example Jira ticket: GR-43593)
- upgrade the library ([example PR](https://github.com/oracle/truffleruby/pull/2930))
- Ruby constants `UNICODE_VERSION` and `UNICODE_EMOJI_VERSION` should have a proper value now

## How to patch MRI source files when update Ruby

When you update Ruby and import MRI source files (both Ruby and C) it's
a common situation when you need to make a change in a file.

Use `defined?(::TruffleRuby)` for Ruby source code:

```ruby
unless defined?(::TruffleRuby)
  require "rbconfig"
end
```

Use `#ifdef TRUFFLERUBY` for C source code:

```cpp
#ifdef TRUFFLERUBY
VALUE rb_imemo_tmpbuf_auto_free_pointer(void);
#else
static inline VALUE rb_imemo_tmpbuf_auto_free_pointer(void);
#endif
```

## How to introduce a new platform-specific constant in Ruby

You need to access in Ruby code a C constant that has a
platform-specific value. There are a bunch of them, e.g. constants
related to files (`SEEK_SET`, `O_RDONLY`), networking (`INET6`,
`IPPROTO_UDP`) or process signals (`SIGINT`, `SIGCHLD`). They are
available in C, but not in Ruby by default.

Such constant values are stored in platform-specific configuration files
(`src/main/java/org/truffleruby/platform`):

- `LinuxAArch64NativeConfiguration.java`
- `LinuxAMD64NativeConfiguration.java`
- `DarwinAArch64NativeConfiguration.java`
- `DarwinAMD64NativeConfiguration.java`

Example of constant configuration:

```java
// src/main/java/org/truffleruby/platform/DarwinAMD64NativeConfiguration.java
configuration.config("platform.file.O_RDONLY", 0);
configuration.config("platform.file.O_WRONLY", 1);
configuration.config("platform.file.O_RDWR", 2);
```

Use `Truffle::Config[<key>]` to access a configured constant
value:

```ruby
# src/main/ruby/truffleruby/core/file.rb
RDONLY   = Truffle::Config['platform.file.O_RDONLY']
WRONLY   = Truffle::Config['platform.file.O_WRONLY']
RDWR     = Truffle::Config['platform.file.O_RDWR']
```

To have a new constant values be added to the platform-specific
configuration files you should declare a constant in
`tool/generate-native-config.rb` file:

```ruby
# tool/generate-native-config.rb
constants 'file' do |cg|
  cg.include 'fcntl.h'
  cg.include 'sys/stat.h'
  # ...

  cg.consts %w[
    O_RDONLY O_WRONLY O_RDWR
    # ...
  ]
end
```

Run `ruby-generate-native-config-*` CI jobs and copy-paste generated
(and printed) platform specific config files.

## How to decide on what to change - ABI version or ABI check

When you modify C-files (*.c, *.h) or compilation-related Ruby files
(e.g. `lib/truffle/rbconfig.rb`) you should either increase ABI
(Application binary interface) check version if the change doesn't
affect ABI or increase ABI version itself explicitly by modifying one of
the files:

- lib/cext/ABI_version.txt
- lib/cext/ABI_check.txt

ABI change is:
- changing headers or compilation flags
- removing/adding a non-static function
- implementing already declared non-static functions

## How to choose where to add new specs - in TruffleRuby or in RubySpec repository

When you want to add specs there are two options - you can add them in:

-   in `spec/ruby` directory in the TruffleRuby repository, or
-   in the RubySpec git repository instead
    ([https://github.com/ruby/spec](https://github.com/ruby/spec))

The `spec/ruby` directory contains the whole RubySpec test suite.

A rule of thumb is that - small changes are committed in the
`spec/ruby` directory but the large changes - in RubySpec directly to
avoid conflicts with changes in RubySpec itself.

The `spec/ruby` is being [synchronized with RubySpec
repository](https://github.com/ruby/spec#synchronization-with-ruby-implementations)
periodically. RubySpec itself is synchronized with other
Ruby implementations (CRuby, JRuby etc). So no manual actions are
required if you add new specs in the `spec/ruby` directory.

## How to choose between Ruby and Java when implement a Ruby Core Library class method

"Prefer Ruby if possible (easier to maintain & read), Java can make
sense in some cases if it can gain much in terms of performance (e.g.,
extra inline cache or so which is not possible in Ruby)". Benoit
Daloze.

It's also an option to implement a method partially in Java and
partially in Ruby. It makes sense when there is complex logic but it's
a performance critical method.

So performance critical code paths are implemented in Java and complex
but less critical paths are implemented in Ruby as follback
specializations.

Let's look at the implementation of the `Float#round` Ruby method:

```java
// FloatNodes.java
@CoreMethod(names = { "==", "===" }, required = 1)
public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

    @Specialization
    protected boolean equal(double a, long b) {
        # ...
    }

    @Specialization
    protected boolean equal(double a, double b) {
        # ...
    }

    @Specialization
    protected boolean equal(double a, RubyBignum b) {
        # ...
    }

    @Specialization(guards = "!isRubyNumber(b)")
    protected Object equal(VirtualFrame frame, double a, Object b) {
        # ...
        return fallbackCallNode.call(a, "equal_fallback", b);
    }
}
```

The helper method `equal_fallback` is implemented as a private method
of the `Float` class.

```ruby
# core/float.rb
def equal_fallback(other)
  # ...
  b, a = math_coerce(other)
  a == b
end
private :equal_fallback
```

## How to write specs for C API

Ruby provides a C API and public functions tested as well as Ruby Core
library.

Specs are located in `spec/ruby/optional/capi` directory and are
written in Ruby.

To make a public C function available in Ruby spec a helper Ruby class
should be introduced. Each method is implemented in C and calls a public
Ruby API function.

Let's consider an example with the `rb_Array(object)` C function.

```ruby
# spec/ruby/optional/capi/array_spec.rb
describe "C-API Array function" do
  before :each do
    @s = CApiArraySpecs.new
  end

  describe "rb_Array" do
    it "returns obj if it is an array" do
      arr = @s.rb_Array([1,2])
      arr.should == [1, 2]
    end

    # ...
  end
end
```

The class `CApiArraySpecs` is declared in C in the following way:

```c
// spec/ruby/optional/capi/ext/array_spec.c
void Init_array_spec(void) {
  VALUE cls = rb_define_class("CApiArraySpecs", rb_cObject);
  rb_define_method(cls, "rb_Array", array_spec_rb_Array, 1);
  // ...
}
```

When a `CApiArraySpecs#rb_Array` Ruby method is called in a test - a
`array_spec_rb_Array` C function will be called.

Such helper C functions usually are simple enough and just call a Ruby C
API function:

```c
// spec/ruby/optional/capi/ext/array_spec.c
static VALUE array_spec_rb_Array(VALUE self, VALUE object) {
  return rb_Array(object);
}
```

When you add specs for a new C API function that was introduced in the
recent Ruby release and doesn't present in previous Ruby versions that
are still maintained such specs will fail on such previous Ruby versions
on a helper functions compilation step.

So Ruby specs should be covered with a `ruby_version_is` guard and
**all the related C code** in specs should be disabled in similar way
with C macroses `RUBY_VERSION_IS_<X>_<Y>` (e.g.
`RUBY_VERSION_IS_3_2`) that present if Ruby version is X.Y (3.2 in
our case) or newer:

```c
#ifdef RUBY_VERSION_IS_3_1
  rb_define_method(cls, "rb_io_maybe_wait_writable", io_spec_rb_io_maybe_wait_writable, 3);
  rb_define_method(cls, "rb_io_maybe_wait_readable", io_spec_rb_io_maybe_wait_readable, 4);
  rb_define_method(cls, "rb_io_maybe_wait", io_spec_rb_io_maybe_wait, 4);
#endif
```

C helpers are defined in `spec/ruby/optional/capi/ext/rubyspec.h`

If a C helper should be more sophisticated - then some Ruby C macroses
and functions might be helpful:

- `Qnil`, `Qtrue`, `Qfalse` - Ruby `nil`, `true` and
  `false` objects
- `INT2NUM`, `INT2FIX` - to convert C `int` to Ruby `Integer`
- `NUM2INT`, `FIX2INT` - to convert Ruby `Integer` to C `int`
- `NUM2LONG`, `FIX2LONG` - to convert Ruby `Integer` to C
  `long`
- `NIL_P` - check whether argument is Ruby `nil`
- `RTEST` - check the "truthy" value (`nil`, `false` - 0,
  anything else - 1)
- `ID2SYM`, `SYM2ID`
- `rb_str_new`, `rb_str_new2`
- `rb_intern`

## How to exclude/include a RubySpec/MRI test case

There is a mechanism to skip tests for not yet implemented functionality
or known but not fixed bug.

### RubySpec tests

Test cases that shouldn't be run by the `jt test` command are tagged
with a tag "fails". Tag files are located in the `spec/tags`
directory.

Example of such tagged test case:

```
# spec/tags/core/array/take_tags.txt
fails:Array#take returns a subclass instance for Array subclasses
```

Use command `jt tag` to tag failing specs:

```
jt tag <path-to-spec-file>
```

Use command `jt untag` to remove passing specs from a tags file

```
jt untag <path-to-spec-file>
```

Use `jt help` command for further details.

### MRI tests

MRI test cases that shouldn't be run by `jt test` command are listed
in the exclude files, that are located in `test/mri/excludes`
directory.

Example of such excluded test case:

```ruby
# test/mri/excludes/Test_Symbol/TestType.rb
exclude :test_check_id_invalid_type, "needs investigation"
```

Use command `jt retag` to re-create an exclude file with all the
failed test cases listed:

```
jt retag <path-to-MRI-test-file>
```

If you need to skip the whole file with MRI tests - it should be added
to `test/mri/failing.exclude` file.

More details are explained in the MRI Tests Guide
(<https://github.com/oracle/truffleruby/blob/master/doc/contributor/mri-tests.md>).

## How to tag slow RubySpec tests

Some test cases are tagged as "slow" to be able to run only fast
specs.

To automatically add tag "slow" for a test case run the following
Shell command:

```
jt test fast <path-to-spec-file>
```

## How to introduce a constant in specs

It's a common issue when to write a new spec you need a new Ruby
constant or not anonymous class/module.

It's prohibited to introduce a new top-level constant to not pollute
the global namespace and keep specs isolated as much as possible.

The only way to define a new constant or not anonymous class/module is
to define it in one of the spec helper modules. Such modules follow a
name convention `...Spec` and usually created per each test file in
`fixtures/classes.rb` file.

This is a helper module for Array specs:

```ruby
# spec/ruby/core/array/fixtures/classes.rb
module ArraySpecs
  SampleRange = 0..1000

  # ...

  class MyArray < Array
   # ...
  end
  
  # ...
end
```

## How to add a new spec

TruffleRuby uses the RubySpec test suite
(<https://github.com/ruby/spec>). Tests are located in the `spec/ruby` directory.

The are the following main directories:

- spec/ruby/core - specs for the Core Library
- spec/ruby/library - specs for the Standard Library
- spec/ruby/language - specs for the Ruby syntax itself