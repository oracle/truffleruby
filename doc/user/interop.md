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
* [Import and export](#import-and-export)

## How Ruby responds to messages

### `IS_EXECUTABLE`

Returns true only for instances of `Method` and `Proc`.

### `EXECUTE`

Calls either a `Method` or `Proc`, passing the arguments as you'd expect.
Doesn't pass a block.

### `INVOKE`

Calls the method with the name you provided, passing the arguments as you'd
expect. Doesn't pass a block.

### `HAS_SIZE`

Returns true if the object responds to `size`.

### `GET_SIZE`

Call `size` on the object.

### `IS_BOXED`

Returns true only for instances of `FFI::Pointer` and `String` with a length of
1, which allows them to be unboxed as a character, or objects that respond to
`unbox`.

### `UNBOX`

For a `String`, returns the first character. Unboxing empty strings is not
supported and will cause an `UnsupportedMessageException`. For a `FFI::Pointer`
returns its address. For all other objects calls `unbox`.

### `IS_NULL`

Returns true only for the `nil` object.

### `KEYS`

If the receiver is a Ruby `Hash`, return the hash keys.

`KEYS(hash)` → `hash.keys`

Otherwise, return the instance variable names, without the leading `@`.

`KEYS(other)` → `other.instance_variables.map { |key| key[1..-1 }`

In both cases the keys are returned as a Ruby `Array` containing Java `String`
objects.

### `READ`

The name must be a Java `int` or `String`, or a Ruby `String` or `Symbol`.

If the receiver is a Ruby `String` and the name is an integer, read a byte from
the string, ignoring the encoding. If the index is out of range you'll get 0:

`READ(string, integer)` → `string.get_byte(integer)`

Otherwise, if the name starts with `@`, read it as an instance variable:

`READ(object, "@name")` → `object.instance_variable_get("name")`

Otherwise, if there isn't a method defined on the object with the same name as
the name, and there is a method defined on the object called `[]`, call `[]`
with the name as the argument:

`READ(object, name)` → `object[name]` unless `object.responds_to?(name)`

Otherwise, perform a method call using the name as the called method name:

`READ(object, name)` → `object.name` if `object.responds_to?(name)`

In all cases where a call is made no block is passed.

### `WRITE`

The name must be a Java `String`, or a Ruby `String` or `Symbol`.

If the name starts with `@`, write it as an instance variable:

`WRITE(object, "@name", value)` → `object.instance_variable_set("name", value)`

Otherwise, if there isn't a method defined on the object with the same name as
the name, and there is a method defined on the object called `[]=`, call `[]=`
with the name and value as the two arguments:

`WRITE(object, name, value)` → `object[name] = value` unless
`object.responds_to?(name)`

Otherwise, perform a method call using the name appended with `=` as the called
method name, and the value as the argument:

`WRITE(object, name, value)` → `object.name = value` if
`object.responds_to?(name + "=")`

In all cases where a call is made no block is passed.

## How to explicitly send messages from Ruby

### `IS_EXECUTABLE`

`Truffle::Interop.executable?(value)`

### `EXECUTE`

`Truffle::Interop.execute(receiver, *args)`

### `INVOKE`

`Truffle::Interop.invoke(receiver, name, *args)`

`name` can be a `String` or `Symbol`.

### `HAS_SIZE`

`Truffle::Interop.size?(value)`

### `GET_SIZE`

`Truffle::Interop.size(value)`

### `IS_BOXED`

`Truffle::Interop.boxed?(value)`

### `UNBOX`

`Truffle::Interop.unbox(value)`

### `IS_NULL`

`Truffle::Interop.null?(value)`

### `KEYS`

`Truffle::Interop.keys(value)`

JRuby will convert the returned value from a foreign object of Java `String`
objects, to a Ruby `Array` of Ruby `String` objects.

### `READ`

`Truffle::Interop.read(object, name)`

If `name` is a `String` or `Symbol` it will be converted into a Java `String`.

### `WRITE`

`Truffle::Interop.read(object, name, value)`

If `name` is a `String` or `Symbol` it will be converted into a Java `String`.

## How to send messages using idiomatic Ruby

### `IS_EXECUTABLE`

Not supported.

### `EXECUTE`

`object.call(*args)`

### `INVOKE`

`object.name(*args)`

`object.name!` if there are no arguments (otherwise it is a `READ`)

### `HAS_SIZE`

Not supported.

### `GET_SIZE`

Not supported.

### `IS_BOXED`

Not supported.

### `UNBOX`

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

`object.name = value` (`#name=(value)`) sends `WRITE`

`object.call(*args)` sends `EXECUTE`

`object.nil?` sends `IS_NIL`

`object.name(*args)` sends `INVOKE` (with no arguments it sends `READ`)

`object.name!` sends `INVOKE`

## Import and export

`Truffle::Interop.export(:name, value)`

`Truffle::Interop.export_method(:name)` (looks for `name` in `Object`)

`value = Truffle::Interop.import(:name)`

`Truffle::Interop.import_method(:name)` (defines `name` in `Object`)

## Interop Eval

`Truffle::Interop.eval(mime_type, source)`
