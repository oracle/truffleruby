# Refinements Implementation Details

This document provides an overview TruffleRuby's refinements implementation.

## `refine` Implementation

This example creates a refinement `R` under the module `M` for the refined class `C`.
The method `refine` is used inside a `Module` to create a refinement.

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
`R` is the refinement module and contains the method definitions from the refine block.
Each method added in a refinement module also gets defined in the refined class `C` with a *refined* flag.
If the refined class `C` contains an existing method with the same name, it is chained as the *original method* of the refined method.
This makes it easy to account for refined methods in method lookup.

Next, `refine` puts the new `R` module into module `M`'s refinements tables, which is a map of refined classes to refinement modules.
This specific entry will contain the key `C` (refined class) and the value `R` (anonymous refinement module).

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

The `using` method makes the refinements in module M active in the lexical scope that follows its call.
It does so by appending module `M`'s refinements to the caller frame's `DeclarationContext`.

The `two` method in the `Test` module will save the frame's `DeclarationContext` when defined.
When the `two` method is called, it will place its `DeclarationContext` into the frame so the refinements are applied to calls in the method body.

Due to the lexical nature of refinements, refinements never change for a given method and
so the DeclarationContext can be looked up only once and doesn't need to be checked after.

## Method Dispatch

During method lookup, if a *refined* method is found, the `DeclarationContext` (from the frame)
is consulted to see if refinements apply to that specific call.
This *refined* flag enables to only check methods which have refinements for refinements and not every module in the ancestor chain.
The same happens for `super` calls.

## References
- [Refinements Spec](https://bugs.ruby-lang.org/projects/ruby-trunk/wiki/RefinementsSpec)
- [Refinements Docs](https://ruby-doc.org/core-2.3.0/doc/syntax/refinements_rdoc.html)
- [Module#refine](https://ruby-doc.org/core-2.3.0/Module.html#method-i-refine)
- [Module#using](https://ruby-doc.org/core-2.3.0/Module.html#method-i-using)
