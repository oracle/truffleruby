# Refinements Implementation Details

This document provides an overview TruffleRuby's refinements implementation.

## `refine` Implementation

This example creates a refinement `R` under the module `M` for the refined class
`C`. The method `refine` is used inside a `Module` to create a refinement.

```ruby
class C; end # C is the refined module or class

module M # M is the namespace module
  R = refine C do # R is the refinement module
    def foo # is the refined method
      "foo"
    end
  end
end

C.foo
#=> UndefinedMethod

using M # activate the refinements from the namespace
C.foo
#=> "foo"
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

During method lookup, if active refinements are found in the the `DeclarationContext` (from the frame),
then we check if any of the ancestors has refinements.
For each refined module, method lookup recursively searches for a refinement method in `[R, ...Rn]`'s ancestors.
To avoid repeated calculations, and to not search C and above before other refinements,
we stop the search for the ancestors of `R` when `C` is found and proceed to the next active refinement.

If nothing was found in active refinements, then the lookup will continue with default behavior and will search in `C` and its ancestors.

The lookup order:
```ruby
R1 -> A -> B -> R2 -> D -> E -> C -> ...
```

## Super Dispatch


The `super` lookup [works in two modes](https://bugs.ruby-lang.org/issues/16977):

1. If `super` is called from a method is directly in R, then we should search in `C` ancestors and ignore other active refinements.
2. If `super` is called from a method placed in a module which included to R, then we should search over all active refinements (as we do for a regular lookup).

Additionally, `super` has access to the caller active refinements, so we use `InternalMethod#activeRefinements` to keep and re-use necessary refinements.

## References

- [Refinements Spec](https://bugs.ruby-lang.org/projects/ruby-trunk/wiki/RefinementsSpec)
- [Refinements Docs](https://ruby-doc.org/core-2.7.0/doc/syntax/refinements_rdoc.html)
- [Module#refine](https://ruby-doc.org/core-2.7.0/Module.html#method-i-refine)
- [Module#using](https://ruby-doc.org/core-2.7.0/Module.html#method-i-using)
