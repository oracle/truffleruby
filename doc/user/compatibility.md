# Compatibility

TruffleRuby aims to be highly compatible with the standard implementation of
Ruby, MRI, version 2.3.3.

Our policy is to match the behaviour of MRI, except where we do not know how to
do so with good performance for typical Ruby programs. Some features work but
will have very low performance whenever they are used and we advise against
using them on TruffleRuby if you can. Some features are missing entirely and may
never be implemented. In a few limited cases we are deliberately incompatible
with MRI in order to provide a greater capability.

In general, we are not looking to debate whether Ruby features are good, bad, or
if we could design the language better. If we can support a feature, we will do.

In the future we aim to provide compatibility with extra functionality provided
by JRuby, but at the moment we do not.

## Features entirely missing

#### Continuations and `callcc`

Continuations and `callcc` are unlikely to ever be implemented in TruffleRuby,
as their semantics fundamentally do not match the technology that we are using.

#### Fork

You cannot `fork` the TruffleRuby interpreter. The feature is unlikely to ever
be supported when running on the JVM, but could be supported in the future on
the SVM.

#### Refinements

We intend to implement refinements, and don't believe that they pose major
technical challenge, but the need for them seems relatively low compared to
other priorities.

#### C extensions

C extensions are a priority for development, and we have a clear technical way
ahead to achivement support for them, but they aren't ready for use yet.

## Features with major differences

#### Threads run in parallel

In MRI, threads are scheduled concurrently but not in parallel. In TruffleRuby
threads are scheduled in parallel. As in JRuby and Rubinius, you are responsible
for correctly synchronising access to your own shared mutable data structures,
and we will be responsible for correctly synchronising the state of the
interpreter.

#### Fibers do not have the same performance characteristics as in MRI

Most use cases of fibers rely on them being easy and cheap to start up and
having memory overheads. In TruffleRuby we implement fibers using operating
system threads, so they have the same performance characteristics as Ruby
threads.

#### Some classes marked as internal will be different

MRI provides some classes that are described in the documentation as being only
available on C Ruby (MRI). We implement these classes if it's practical to do
so, but this isn't always the case. For example `RubyVM` is not available.

## Features with subtle differences

#### The range of `Fixnum` is different

The range of `Fixnum` is slightly larger than it is in MRI. This won't be an
issue when we support Ruby 2.4, as `Integer` is unified there.

#### Command line switches

`-y`, `--yydebug`, `--dump=` are ignored with a warning as they are internal
development tools.

`-X` is an undocumented synonym for `-C` and we (and other alternative
implementations of Ruby) have repurposed it for extended options. We warn if
your `-X` options looks like it was actually intended to be as in MRI.

## Features with very low performance

#### Keyword arguments

Keyword arguments don't have *very* low performance, but they are not optimised
as other language features are. You don't need to avoid keyword arguments, but
performance with them may not be what you would hope for.

#### `ObjectSpace`

Using most methods on `ObjectSpace` will temporarily lower the performance of
your program. Using them in test cases and other similar 'offline' operations is
fine, but you probably don't want to use them in the inner loop of your
production application.

#### `set_trace_func`

Using `set_trace_func` will temporarily lower the performance of your program.
As with `ObjectSpace`, we would recommend that you do not use this in the inner
loop of your production application.

## Compatibility with JRuby

#### Ruby to Java interop

Calling Java code from Ruby (normal Java code, not JRuby's Java extensions which
are covered below) is in development but not ready for use yet. We aim to
provide the same interface as JRuby does for this functionality.

#### Java to Ruby interop

Calling Ruby code from Java is supported by the `PolyglotEngine` API of Truffle.

http://lafo.ssw.uni-linz.ac.at/javadoc/truffle/latest/com/oracle/truffle/api/vm/PolyglotEngine.html

In the future we hope to also support the standard JSR 223, Scripting for the
Java Platform, specification and the `ScriptEngineManager` class, which is also
supported by JRuby. We are unlikely to ever implement JRuby's own scripting
interface such as the `Ruby` and `RubyInstanceConfig` classes.

#### Java extensions

Use Java extensions written for JRuby is not supported. We could apply the same
techniques as we have developed to run C extensions to this problem, but it's
not clear if this will ever be a priority.

## Compatibility with Rubinius

We do not have any plans at the moment to provide support for Rubinius'
extensions to Ruby.

In some cases we provide some of the classes available under the `Rubinius`
module for historical reasons, but these may behave differently or be removed
in future versions.
