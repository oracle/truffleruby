# Polyglot Programming

## Evaluating code in foreign languages

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

## Using Ruby objects a foreign language

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
