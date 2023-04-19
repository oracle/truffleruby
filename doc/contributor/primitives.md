# Primitives

## Naming

Primitives are generally named `#{module or class to which it belongs}_#{name of the operation}` such as `string_start_with?`.

The `object_` prefix should only be used for instance variables-related operations, like `object_ivars`.

For primitives which are not specific to a module or class, use no prefix, such as `Primitive.is_a?`/`Primitive.equal?`.

For primitives used in many places it is nice to have a shorter name.
OTOH for primitives used in a single place, e.g., to implement part of the logic in Ruby, then a longer name is fine.
