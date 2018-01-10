# Truffle Interop

TruffleRuby supports standard Truffle interop messages. This document explains
what it does when it receives them, how to get it to explicitly send them, how
to get it to send them using more idiomatic Ruby, and how what messages it sends
for normal Ruby operations on foreign objects.

Interop ignores visibility entirely.

* [How Ruby responds to messages](#how-ruby-responds-to-messages)
* [How to explicitly send messages from Ruby](#how-to-explicitly-send-messages-from-ruby)
* [How to send messages using idiomatic Ruby](#how-to-send-messages-using-idiomatic-ruby)
* [What messages are sent for Ruby syntax on foreign objects](#what-messages-are-sent-for-ruby-syntax-on-foreign-objects)
* [String conversion](#string-conversion)
* [Import and export](#import-and-export)
* [Interop eval](#interop-eval)
* [Additional methods](#additional-methods)
* [Notes on method resolution](#notes-on-method-resolution)
* [Threading and interop](#threading-and-interop)

## How Ruby responds to messages

### `IS_EXECUTABLE`

Returns true only for instances of `Method` and `Proc`.

### `EXECUTE`

Calls either a `Method` or `Proc`, passing the arguments as you'd expect.
Doesn't pass a block.

### `INVOKE`

Calls the method with the name you provided, passing the arguments as you'd
expect. Doesn't pass a block.

### `IS_INSTANTIABLE`

Returns if the object responds to `new`.

### `NEW`

Calls `new`, passing the arguments as you'd expect.

### `HAS_SIZE`

Returns true if the object responds to `size`.

### `GET_SIZE`

Call `size` on the object.

### `IS_BOXED`

Returns true only for instances of `String`, `Symbol`,
`Truffle::FFI::Pointer` and objects that respond to `unbox`.

### `UNBOX`

For a `String` or `Symbol`, produces a `java.lang.String`, similar to
`Truffle::Interop.to_java_string`. For a `Truffle::FFI::Pointer`,
produces the address as a `long`. For all other objects calls `unbox`.

### `IS_POINTER`

Returns true only for `FFI::Pointer`.

### `AS_POINTER`

Calls `address` if the object responds to it, otherwise throws
`UnsupportedMessageException`.

### `TO_NATIVE`

Calls `to_native` if the object responds to it, otherwise throws
`UnsupportedMessageException`.

### `IS_NULL`

Returns true only for the `nil` object.

### `HAS_KEYS`

Returns true for all objects except those primitives that are always frozen,
`nil`, `true`, `false`, `Fixnum`, `Bignum`, `Float`, `Symbol`. Note that `KEYS`
will continue to return an empty list, even if `HAS_KEYS` returns false.

### `KEYS`

If the receiver is a Ruby `Hash`, return the hash keys.

`KEYS(hash)` → `hash.keys`

Otherwise, return the instance variable names, without the leading `@`.

`KEYS(other)` → `other.instance_variables.map { |key| ...key without @... }`

In both cases if the `internal` flag is set, instance variable names with the
leading `@` are also returned.

Keys are returned as a Ruby `Array` containing Ruby `String` objects.

### `KEY_INFO`

If the object is a Ruby `Hash`, and the key is in the hash, `READABLE`,
`WRITABLE` are set. If the key is not in the hash, neither are set. Note that
the interface for `KEY_INFO` converts the key to a Java `String`, so if your
keys are `Symbol`, they will not match. `INTERNAL` will never be set.

If the object is not a Ruby `Hash`:

`READABLE` is set if the object responds to a method of the same name.

`WRITABLE` is set if the object responds to a method of the same name appended
with `=`.

For all objects:

`EXISTING` is set if if either `READABLE` or `WRITABLE` are set.

`INVOCABLE` is never set, because currently `KEYS` does not include methods.

`READABLE`, `WRITABLE`, and `INTERNAL` will be set for names with a leading `@`,
if there is an instance variable with that name.

### `READ`

The name must be an `int` (small Ruby `Fixnum`), or a Ruby `String` or `Symbol`,
or a Java `String`.

If the receiver is a Ruby `String` and the name is an integer, read a byte from
the string, ignoring the encoding. If the index is out of range you'll get 0:

`READ(string, integer)` → `string.get_byte(integer)`

Otherwise, if the name starts with `@`, read it as an instance variable:

`READ(object, "@name")` → `object.instance_variable_get("name")`

Otherwise, if there is a method defined on the object with the same name as
the name, perform a method call using the name as the called method name:

`READ(object, name)` → `object.name` if `object.responds_to?(name)`

Otherwise, if there isn't a method defined on the object with the same name as
the name, and there is a method defined on the object called `[]`, call `[]`
with the name as the argument:

`READ(object, name)` → `object[name]` unless `object.responds_to?(name)`

Otherwise throws `UnknownIdentifierException`.

In all cases where a call is made no block is passed.

An exception during a read will result in an `UnknownIdentifierException`.

### `WRITE`

The name must be a `String` or `Symbol`.

If the name starts with `@`, write it as an instance variable:

`WRITE(object, "@name", value)` → `object.instance_variable_set("name", value)`

Otherwise, if there is a method defined on the object with the same name as
the name appended with `=`, perform a method call using the name appended with
`=` as the called method name, and the value as the argument:

`WRITE(object, name, value)` → `object.name = value` if
`object.responds_to?(name + "=")`

Otherwise, if there isn't a method defined on the object with the same name as
the name appended with `=`, and there is a method defined on the object called
`[]=`, call `[]=` with the name and value as the two arguments:

`WRITE(object, name, value)` → `object[name] = value` if
`object.responds_to?("[]=")` and unless
`object.responds_to?(name + "=")`

Otherwise throws `UnknownIdentifierException`.

In all cases where a call is made no block is passed.

## How to explicitly send messages from Ruby

### `IS_EXECUTABLE`

`Truffle::Interop.executable?(value)`

### `EXECUTE`

`Truffle::Interop.execute(receiver, *args)`

`Truffle::Interop.execute_without_conversion(receiver, *args)`

### `INVOKE`

`Truffle::Interop.invoke(receiver, name, *args)`

`name` can be a `String` or `Symbol`.

### `IS_INSTANTIABLE`

`Truffle::Interop.instantiable?(receiver)`

### `NEW`

`Truffle::Interop.new(receiver, *args)`

### `HAS_SIZE`

`Truffle::Interop.size?(value)`

### `GET_SIZE`

`Truffle::Interop.size(value)`

### `IS_BOXED`

`Truffle::Interop.boxed?(value)`

### `UNBOX`

`Truffle::Interop.unbox(value)`

`Truffle::Interop.unbox_without_conversion(value)`

### `IS_POINTER`

`Truffle::Interop.pointer?(value)`

### `AS_POINTER`

`Truffle::Interop.as_pointer(value)`

### `TO_NATIVE`

`Truffle::Interop.to_native(value)`

### `IS_NULL`

`Truffle::Interop.null?(value)`

### `HAS_KEYS`

`Truffle::Interop.keys?(value)`

### `KEYS`

`Truffle::Interop.keys(value, internal=false)`

TruffleRuby will convert the returned value from a foreign object of Java
`String` objects, to a Ruby `Array` of Ruby `String` objects.

### `KEY_INFO`

`Truffle::Interop.key_info(object, name)`

Returns an array containing zero or more of the symbols
`[:existing, :readable, :writable, :invocable, :internal]` in an undefined
order.

### `READ`

`Truffle::Interop.read(object, name)`

### `WRITE`

`Truffle::Interop.read(object, name, value)`

## How to send messages using idiomatic Ruby

### `IS_EXECUTABLE`

Not supported.

### `EXECUTE`

`object.call(*args)`

### `INVOKE`

`object.name(*args)`

`object.name!` if there are no arguments (otherwise it is a `READ`)

### `IS_INSTANTIABLE`

`object.respond_to?(:new)`

### `NEW`

`object.new(*args)`

### `HAS_SIZE`

Not supported.

### `GET_SIZE`

Not supported.

### `IS_BOXED`

Not supported.

### `UNBOX`

Not supported.

### `IS_POINTER`

Not supported.

### `AS_POINTER`

Not supported.

### `TO_NATIVE`

Not supported.

### `IS_NULL`

`value.nil?`

### `KEYS`

Not supported.

### `READ`

`object.name`

`object[name]`, where name is a `String` or `Symbol` in most cases, or an
integer, or anything else

### `WRITE`

`object.name = value`

`object[name] = value`, where name is a `String` or `Symbol` in most cases, or
an integer, or anything else

## What messages are sent for Ruby syntax on foreign objects

`object[name]` (`#[](name)`) sends `READ`

`object.name` with no arguments send `READ`

`object[name] = value` (`#[]=(name, value)`) sends `WRITE`

`object.name = value` (a message name matching `.*[^=]=`, such as `name=`, and with just one argument) sends `WRITE`

`object.call(*args)` sends `EXECUTE`

`object.nil?` sends `IS_NIL`

`object.name(*args)` sends `INVOKE` (with no arguments it sends `READ`)

`object.name!` sends `INVOKE`

`object.new(*args)` sends `NEW`

`object.respond_to?` calls `Truffle::Interop.respond_to?(object, message)`,
which supports `to_a`, `to_ary`, `new` and returns `false` for everything else.

`object.to_a` and `object.to_ary` calls `Truffle::Interop.to_array(object)`

`object.equal?(other)` returns whether `object` is the same as `other` using reference equality, like `BasicObject#equal?`

`object.inspect` produces a simple string of the format
`#<Truffle::Interop::Foreign:system-identity-hash-code>`

`object.__send__(name, *args)` works in the same way as literal method call on the
foreign object, including allowing the special-forms listed above (see
[notes on method resolution](#notes-on-method-resolution)).

## String conversion

Ruby strings and symbols are unboxable to Java strings.

A call from Ruby to a foreign language using `NEW`, `EXECUTE`, `INVOKE`, `READ`,
or `WRITE`, that has Ruby strings or symbols as arguments, will convert each
Ruby string or symbol argument to a Java string. You can avoid this conversion
for `EXECUTE` using `Truffle::Interop.execute_without_conversion`, and for
`UNBOX` using `Truffle::Interop.unbox_without_conversion`.

A call from Ruby to a foreign language using `NEW`, `EXECUTE`, `INVOKE`, `READ`,
`WRITE`, or `UNBOX`, that returns a Java string will convert the returned string
to a Ruby string.

A call from a foreign language to Ruby using `NEW`, `EXECUTE`, `INVOKE`, or
`WRITE`, that has Java strings as arguments, will convert each Java string
argument to a Ruby string.

It is planned that Java strings, and boxed foreign strings (foreign objects that
respond positively to `IS_BOXED` and `UNBOX` to a Java string), will be able to
be used in all locations where a Ruby string could, and will be converted to a
Ruby string at that point, but this is not implemented yet.

## Import and export

`Truffle::Interop.export(:name, value)`

`Truffle::Interop.export_method(:name)` (looks for `name` in `Object`)

`value = Truffle::Interop.import(:name)`

`Truffle::Interop.import_method(:name)` (defines `name` in `Object`)

## Interop Eval

`Truffle::Interop.eval(mime_type, source)`

`Truffle::Interop.import_file(path)` evals an entire file, guessing the correct
language MIME type.

## Additional methods

`Truffle::Interop.foreign?(object)`

`Truffle::Interop.mime_type_supported?(mime_type)` reports if a language's MIME
type is supported for interop.

`Truffle::Interop.java_string?(object)`

`Truffle::Interop.to_java_string(ruby_string)`

`Truffle::Interop.from_java_string(java_string)`

`Truffle::Interop.object_literal(a: 1, b: 2, c: 3...)` gives you a simple object
with these fields and values, like a JavaScript object literal does. You can
then continue to read and write fields on the object and they will be
dynamically added, similar to `OpenStruct`.

`Truffle::Interop.enumerable(object)` gives you an `Enumerable` interface to a
foreign object.

`Truffle::Interop.to_java_array(array)` gives you a proxied Java array copied
from the Ruby array.

`Truffle::Interop.java_array(a, b, c...)` a literal variant of the former.

`Truffle::Interop.deproxy(object)` deproxy a Java object if it has been proxied.

`Truffle::Interop.to_array(object)` converts to a Ruby array by calling
`GET_SIZE` and sending `READ` for each index from zero to the size.

`Truffle::Interop.respond_to?(object, name)` sends `HAS_SIZE` for `to_a` or
`to_ary`, or `false` otherwise. Note that this means that many interop objects
may have methods you can call that they do not report to respond to.

`Truffle::Interop.meta_object(object)` returns the Truffle meta-object that
describes the object (unrelated to the metaclass).

## Notes on method resolution

Method calls on foreign objects are usually translated exactly into foreign
`READ`, `INVOKE` and other messages. The other methods listed in
[what messages are sent for Ruby syntax on foreign objects](#what-messages-are-sent-for-ruby-syntax-on-foreign-objects)
are a kind of special-form - they are implemented as a special case in the
call-site logic. They are not being provided by `BasicObject` or `Kernel` as you
may expect. This means that for example `#method` isn't available, and you can't
use it to get the method for `#to_a` on a foreign object, as it is a
special-form, not a method.

## Threading and interop

TruffleRuby is by default a multi-threaded language. This may be incompatible
with other Truffle languages, so you can disable the creation of multiple
threads with the option `-Xsingle_threaded`, or
`-Dtruffleruby.single_threaded=true` from another launcher.

When this option is enabled, the `timeout` module will warn that the timeouts
are being ignored, and signal handlers will warn that a signal has been caught
but will not run the handler, as both of these features would require starting
new threads.
