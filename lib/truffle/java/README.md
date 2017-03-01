# JRuby-compatible Java interop
JRuby provides a rich set of interop functionality between Ruby and
Java, and we attempt to emulate it faithfully in TruffleRuby.
## Ruby proxies for Java classes
In general a Ruby programmer using the interop API will never see raw
Java objects, but instead either see primitive types which have been
converted (numeric types and string), or other types wrapped in a
proxy class. These classes are organised under the `::Java` module
according to their Java package. For example `java.lang.Object` would
have a proxy class created as `::Java::JavaUtil::Object`. To make it
feel more natural when accessing Java classes they can also be
accessed using `Kernel#java` to return the `java` package, with
sub-package objects created as necessary, so you can write
`java.lang.Object.new` to create a new instance of an object. In a
similar fashion interfaces will have a corresponding module
created. An interface's module will be included in the proxy of any
Java class that implements that interface, and a proxy will be a
subclass of the proxy class of its Java super class.
### Building proxies
Building proxies classes requires that a class be created (if one does
not already exist) and methods and constants added to it representing
the methods and fields on the Java class. Static Java fields and
methods will cause singleton methods to be created on the Ruby proxy
class, and final static fields will cause constants to be
created. Non-static Java fields and methods will cause instance
methods to be created in Ruby. This work is split between the
`JavaUtilities` module which keeps track of the created proxy classes
and does the initial class creation and `ProxyBuilder`s which take care
of adding methods, ancestors for interfaces, etc.

The Ruby methods created will be lambdas that delegate the work to a
`JavaDispatcher` as some work may need to be done both selecting the
appropriate method and performing type conversion.
## Method dispatch
JRuby attempts to find the most appropriate Java method to call for
any set of arguments it is given, and we do not perfectly replicate
all of these processes yet. The work of finding the best method and
building type converters and type checkers that may be reused later is
done by `MethodSelector`, while the actual call is performed by a
`JavaDispatcher`. The selection algorithm works roughly as follows:
1. Find all methods with an arity compatible with the arguments. This
   means all fixed arity methods which take precisely that many
   arguments, and all variable arity methods which take less than or
   equal to that number.
2. Now for each argument check if it can be converted to the
   corresponding parameter type for each method, and discard any
   methods where this conversion cannot be done.
3. If no candidates remain then other type conversion checks may be
   attempted such as turning Ruby `Proc`s into interface types.
4. If a single candidate is left then that method will be called.
5. If several candidates are left then attempt to find the one with
   the most specific types and call that.
## Exception handling
JRuby allows ruby code to raise and rescue pure Java exceptions and
for standard rescue blocks to rescue Java exceptions, and we do not
yet perfectly reproduce this functionality. It is done using several
interacting mechanisms:
1. If a rescue block is for `NativeException` (a subclass of
   `RuntimeError`) then it will catch Java exceptions boxed in
   `NativeException`.
2. If a rescue block is for `Exception` or `StandardError` then it
   will catch a `java.lang.Exception` and its subclasses as `===` on
   `Exception` is defined to match them.
3. A rescue block for a specific Java exception type will match that
   Java exception type or its subclasses. For example
   ```
   rescue java.lang.IndexOutOfBoundsException => e
   ```
   will rescue IndexOutOfBOundsException and any subclass of it.

It is also possible to raise Java exceptions from Ruby code.
## Interfaces and subclasses
In JRuby a Ruby class can implement Java interfaces by including the
appropriate proxy module, or inherit from a Java class by subclassing
it. In TruffleRuby this is implemented using the `inherited` and
`included` Ruby meta-programming hooks to be notified of these events
and constructing a separate Java object to act as the proxy and
forward all calls back to Ruby.
## Extra support for common types in Java and Ruby
JRuby's core implementations of arrays, sets, hashes, and so forth all
implement the standard equivalent Java interface types and so can be
passed easily to Java functions. In TruffleRuby we handle this by
creating specialised proxies for these types and having special cases
for the interfaces types when type checking and converting values.

JRuby also adds many standard Ruby methods to common Java interface
types. This is done both by making their Ruby proxies inherit from
`Enumerable` and similar classes, and by adding methods specifically
to the interface proxies. In JRuby this is done in the Java runtime,
while we implement it entirely in Ruby code.
