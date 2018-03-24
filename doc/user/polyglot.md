# Polyglot Programming

TruffleRuby allows you to interface with any other Truffle language to create
polyglot programs -- programs written in more than one language.

This document describes how to load code written in foreign languages, how to
export and import objects between languages, how to use Ruby objects from
a foreign language, how to use foreign objects from Ruby, and how to load
Java types to interface with Java.

There is also [additional documentation](../contributor/interop.md) that
describes in more depth how polyglot programming in TruffleRuby is implemented
using the Truffle interop API, and exactly how Ruby is matched to this API.

* [Loading code written in foreign languages](#loading-code-written-in-foreign-languages)
* [Exporting Ruby objects to foreign languages](#exporting-ruby-objects-to-foreign-languages)
* [Importing foreign objects to Ruby](#importing-foreign-objects-to-ruby)
* [Using Ruby objects from a foreign language](#using-ruby-objects-from-a-foreign-language)
* [Using foreign objects from Ruby](#using-foreign-objects-from-ruby)
* [Accessing Java objects](#accessing-java-objects)
* [Strings](#strings)
* [Threading and interop](#threading-and-interop)
* [Embedded configuration](#embedded-configuration)

Also see the separate document on
[JRuby-compatible Java interop](jruby-java-interop.md).

## Loading code written in foreign languages

`Polyglot.eval(id, string)` executes code in a foreign language identified by
its ID.

`Polyglot.eval_file(id, path)` executes code in a foreign language from a file,
identified by its language ID.

`Polyglot.eval_file(path)` executes code in a foreign language from a file,
automatically determining the language.

## Exporting Ruby objects to foreign languages

`Polyglot.export(name, value)` exports a value with a given name.

`Polyglot.export_method(name)` exports a method, defined in the top-level
object.

## Importing foreign objects to Ruby

`Polyglot.import(name)` imports and returns a value with a given name.

`Polyglot.import_method(name)` imports a method with a given name, and defines
it in the top-level object.

## Using Ruby objects from a foreign language

Using JavaScript as an example.

`obj[name/index]` calls `[name/index]` on the Ruby object.

`obj[name/index] = value` calls `[name/index] = value` on the Ruby object.

`delete obj.name` calls `delete(name)` on the Ruby object.

`delete obj[name/index]` calls `delete(name)` on the Ruby object.

`obj.length` calls `size` on the Ruby object.

`Object.keys(array)` gives an array of the array indices.

`Object.keys(hash)` gives the hash keys as strings.

`Object.keys(object)` gives the methods of an object as functions.

`object(args...)` calls a Ruby `Proc`, `Method`, `UnboundMethod`, etc.

`object.name(args...)` calls a method on the Ruby object.

`new object(args...)` calls `new(args...)` on the Ruby object.

`"length" in obj` tells you if a Ruby object responds to `size`.

`obj == null` calls `nil?` on the Ruby object.

### Notes on creating Ruby objects for use in foreign languages

If you want to pass a Ruby object to another language for fields to be read and
written, a good object to pass is usually a `Struct`, as this will have both the
`.foo` and `.foo =` accessors for you to use from Ruby, and they will also
respond to `.['foo']` and `.['foo'] =` which means they will work from other
languages sending read and write messages.

## Using foreign objects from Ruby

`object[name/index]` will read a member from the foreign object.

`object[name/index] = value` will write a value to the the foreign object.

`object.delete(name/index)` will remove a value from the foreign object.

`object.size` will get the size or length of the foreign object.

`object.keys` will get an array of the members of the foreign object.

`object.call(*args)` will execute the foreign object.

`object.name(*args)` will invoke a method called `name` on the foreign object.

`object.new(*args)` will create a new object from the foreign object (as if it's
some kind of class.)

`object.respond_to?(:size)` will tell you if the foreign object has a size or
length.

`object.nil?` will tell you if the foreign object represents the language's
equivalent of `null` or `nil`.

`object.respond_to?(:call)` will tell you if a foreign object can be executed.

`object.respond_to?(:new)` will tell you if a foreign object can be used to
create a new object (if it's a class).

`object.respond_to?(:keys)` will tell you if a foreign object can give you a
list of members.

`Polyglot.as_enumerable(object)` will create a Ruby `Enumerable` from the
foreign object, using its size or length and reading from it.

## Accessing Java objects

TruffleRuby's Java interop interface is similar to the interface from the
Nashorn JavaScript implementation, as also implemented by Graal.js. It's only
available in JVM mode (`--jvm`).

`Java.type(name)` returns a Java class object, given a name such as
`java.lang.Integer` or `int[]`.

Also see the separate document on
[JRuby-compatible Java interop](jruby-java-interop.md).

## Strings

Ruby strings and symbols are converted to Java strings when they are passed to
foreign languages, and Java strings are converted to Ruby strings when they
are passed into Ruby.

## Threading and interop

Ruby is designed to be a multi-threaded language and much of the ecosystem
expects threads to be available. This may be incompatible with other Truffle
languages which do not support threading, so you can disable the creation of
multiple threads with the option `-Xsingle_threaded`. This option is set by
default unless the Ruby launcher is used.

It's separate from normal Ruby options and the
[embedded configuration](#embedded-configuration) due technical details in the
design of the Graal SDK.

When this option is enabled, the `timeout` module will warn that the timeouts
are being ignored, and signal handlers will warn that a signal has been caught
but will not run the handler, as both of these features would require starting
new threads.

## Embedded configuration

When used outside of the Ruby launcher, such as from another language's launcher
via interop, embedded using the native polyglot library, or embedded via the
Graal SDK, TruffleRuby will be automatically configured to work more
cooperatively within another application. This includes options such as not
installing an interrupt signal handler, and using the IO streams from the Graal
SDK.

This can be turned off even when embedded, with the `embedded` option
(`--ruby.embedded=false` from another launcher, or
`-Dpolyglot.ruby.embedded=false` from a normal Java application).
