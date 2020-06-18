# Refinements Implementation Details

This document provides an overview TruffleRuby's refinements implementation.

## `refine` Implementation

This example creates a refinement `R` under the module `M` for the refined class
`C`. The method `refine` is used inside a `Module` to create a refinement.

```ruby
class C
  def one
    "one unrefined"
  end
end

module M
  refine C do
    # self is R, the anonymous refinement module
    def one
      "one refined"
    end
  end
end
```

The `refine` block is module eval'ed into a new anonymous module `R`.
`R` is the refinement module and contains:
1. The method definitions from the refine block.
2. Included and prepended modules from the refine block.
3. `C` ancestors. This is useful for `super` lookup, because `C`'s ancestors have a higher priority than, other active refinements.

Next, `refine` puts the new `R` module into module `M`'s refinements tables,
which is a map of refined classes to refinement modules. This specific entry
will contain the key `C` (refined class) and the value `R` (anonymous refinement
module).

## `using` Implementation

```ruby
module Test

  # refinements not active
  using M
  # refinements active

  def two
    C.new.one # => "one refined"
  end

end
```

The `using` method makes the refinements in module M active in the lexical scope
that follows its call. It does so by appending module `M`'s refinements to the
caller frame's `DeclarationContext`.

The `two` method in the `Test` module will save the frame's `DeclarationContext`
when defined. When the `two` method is called, it will place its
`DeclarationContext` into the frame so the refinements are applied to calls in
the method body.

Due to the lexical nature of refinements, refinements never change for a given
method and so the DeclarationContext can be looked up only once and doesn't need
to be checked after.

## Method Dispatch
Example:
```ruby
using M1; # R1.ancestors = [R1, A, B, C, Object, Kernel, ...]
using M2; # R2.ancestors = [R2, D, E, C, Object, Kernel, ...]

C.new.foo # => ?
```

If there are active refinements for the module is found, method lookup recursively iterates by them to find refinement method in `[R, ..Rn]`'s ancestors. To avoid repeated calculations, we stop the search for the ancestors of `R` when `C` is found and proceed to the next active refinement.

If nothing was found in active refinements, then the lookup will continue with default behavior and will search in `C` and its ancestors.

The lookup order:
```ruby
R1 -> A -> B -> R2 -> D -> E -> C -> ...
```

The `super` lookup works in much the same way, except `C` ancestors have a higher priority than other active refinements.

The lookup order for `super`:
```ruby
R1 -> A -> B -> C -> ... -> R2 -> D -> E -> C -> ...
```

## References

- [Refinements Spec](https://bugs.ruby-lang.org/projects/ruby-trunk/wiki/RefinementsSpec)
- [Refinements Docs](https://ruby-doc.org/core-2.7.0/doc/syntax/refinements_rdoc.html)
- [Module#refine](https://ruby-doc.org/core-2.7.0/Module.html#method-i-refine)
- [Module#using](https://ruby-doc.org/core-2.7.0/Module.html#method-i-using)
