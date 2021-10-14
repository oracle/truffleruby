# TypeProf milestones and TODOs

## Big milestones

### Rails support

There are many known issues for the analysis of typical Ruby programs including Rails apps.

* The main difficulty is that they use many language extensions like `ActiveSupport`.
  Some features (for example, `blank?` and `Time.now + 1.day`) are trivial to support,
  but others (for example, `ActiveSupport::Concern` and Zeitwerk) will require special support.
* The other difficulty is that they heavily use meta-programming features like `ActiveRecord`.
  It dynamically defines some methods based on external data (such as DB schema) from the code.

Currently, projects called [`gem_rbs`](https://github.com/ruby/gem_rbs) and [`rbs_rails`](https://github.com/pocke/rbs_rails) are in progress.
The former provides several RBS files for some major gems including Rails.
The latter is a tool to generate RBS prototype of a target Rails application by introspection (executing it and monitoring DB schema, etc).
TypeProf can use their results to improve analysis precision and performance.

What we need to do:

* Experimentally apply TypeProf to some Rails programs and identify problems
* Make TypeProf able to work together with `rbs_rails` for supporting trivial core extensions and `ActiveRecord`.
* Implement special support for some fundamental language extensions of Rails like `ActiveSupport::Concern`.
  (It would be best if TypeProf has a plugin system and if we can factor out the special support as a plugin for Rails.)

### Error detection and diagnosis feature

At present, TypeProf focuses on generation of RBS prototype from no-type-annotated Ruby code.
However, it is possible for TypeProf to report possible errors found during the analysis.
In fact, an option `-v` experimentally shows possible errors found.
There are some reasons why it is disabled by default:

* (1) There are too many false positives.
* (2) Some kind of error reporting is not implemented yet.
* (3) Some reported errors are difficult for a user to understand.

For (1), we will research how we can avoid false positives to support typical Ruby coding patterns as much as possible.
The primary way is to improve the analysis precision, e.g., enhancing flow-sensitive analysis.
If the S/N ratio of an error type is too low, we need to consider to suppress the kind of reports.
Also, we may try allowing users to guide TypeProf to analyze their program well.
(The simplest way is to write inline type casts in the code, but we need to find more Ruby/RBS way.)
We may also explore a "TypeProf-friendly coding style" which TypeProf can analyze well.
(In principle, the plainer code is, the better TypeProf can analyze.)

For (2), currently, TypeProf checks the argument types of a call to a method whose type signature is declared in RBS.
However, it does not check the return type yet. Redefinition of constants should be warned too.
We will survey what errors and warnings TypeProf can print, and evaluate the S/N ratio of each report.

For (3), since TypeProf uses whole program analysis, an error may be reported at a very different place from its root bug.
Thus, if TypeProf shows a possible type error, a diagnosis feature is needed to answer why TypeProf thinks that the error may occur.
TypeProf has already implemented a very primitive diagnosis feature, `Kernel#p`, to check what type an expression has.
Another idea is to create a pseudo backtrace why TypeProf thought the possible type error may occur.
We should consider this feature with LSP support.

### Performance improvement

Currently, TypeProf is painfully slow. Even if a target application is small.

The main reason is that TypeProf analyzes not only the application code but also library code:
if an application requires `"foo"`, TypeProf actually loads `foo.rb` even from a gem,
and furthermore, if `foo.rb` requires `"bar"`, it loads `bar.rb` recursively.

RBS will help to stop this cascade;
when an application requires `"foo"`, TypeProf loads `sig/foo.rbs` instead of `foo.rb` if the `foo` gem contains both.
Such a RBS file is optional for TypeProf but required for Steep.
So, we think many gems will eventually equip their RBS declarations.

That being said, we should continue to improve the analysis performance of TypeProf. We have some ideas.

* Unfortunately, TypeProf often analyzes one method more than once when it accepts multiple types.
  As TypeProf squashes the argument types to a union, this duplicated analysis is not necessarily needed.
  But when TypeProf first analyzes a method, it is difficult to determine if the method will accept another type in further analysis.
  So, we need good heuristics to guess whether a method accepts multiple types or not, and if so, delay its analysis.
* Currently, TypeProf executes the bytecode instructions step by step.
  This requires creating an environment object after each instruction, which is very heavy.
  Many environment creations can be omitted by executing each basic block instead of each instruction.
  (Basic block execution will also make flow-sensitive analysis easier.)
* The slowest calculation in TypeProf is to create an instance of a Type class.
  The creation uses memoization; TypeProf keeps all Type instances created so far, and reuses them if already exist.
  However, it is very heavy to check if an instance already exists or not.
  (Currently, it is very simply implemented by a big Hash table.)
  We've already improved the memoization routine several times but looks like it is still the No.1 bottleneck.
  We need to investigate and try improving more.
* TypeProf heavily uses Hash objects (including above) mainly to represent a set.
  A union of sets is done by `Hash#merge`, which takes O(n).
  A more lightweight data structure may make TypeProf faster.
  (But clever structure often has a big constant term, so we need to evaluate the performance carefully.)
* Reusing an old analysis and incrementally updating it will bring a super big improvement.
  This would be especially helpful for LSP support, so we need to tackle it after the analysis approach is mature.

### Language Server Protocol (LSP) support

In the future, we want TypeProf to serve as a language server to show the result in IDE in real-time.
However, the current analysis approach is too slow for IDE. So we need to improve the performance first.

Even if TypeProf becomes fast enough, its approach has a fundamental problem.
Since TypeProf uses whole program analysis, one edit may cause a cascade of propagation:
if a user write `foo(42)`, an Integer is propagated to a method `foo`,
and if `foo` passes its argument to a method `bar`, it is propagated to `bar`, ...
So, a breakthrough for LSP may be still needed, e.g, limiting the propagation range in real-time analysis,
assuming that a type interface of module boundary is fixed, etc.

## Relatively smaller TODOs

* Support more RBS features
  * TypeProf does not deal with some RBS types well yet.
    * For example, the `instance` type is handled as `untyped.
    * The `self` type is handled well only when it is used as a return type.
    * Using a value of the `void` type should be warned appropriately.
  * RBS's `interface` is supported just like a module (i.e., `include _Foo` is explicitly required in RBS),
    but it should be checked structually (i.e., it should be determined as a method set.)
  * The variance of type parameters is currently ignored.

* Support more Ruby features
  * Some meta-programming features like `Class.new`, `Object#method`, etc.
    * It is possible to support `Class.new` by per-allocation-site approach:
      e.g., In TypeProf, `A = Class.new; B = Class.new` will create two classes, but `2.times { Class.new }` will create one class.
  * The analysis precision can be improved more for some Ruby features like pattern matching, keyword arguments, etc.
    * For example, `foo(*args, k:1)` is currently compiled as if it is `foo(*(args + [{ :k => 1 }]))` into Ruby bytecode.
      This mixes the keyword arguments to a rest array, and makes it difficult for TypeProf to track the keyword arguments.
  * Support Enumerator as an Array-type container.
  * Support `Module#protect` (but RBS does not yet).
  * More heuristics may help such as `==` returns a bool regardless to its receiver and argument types.

* Make TypeProf more useful as a tool
  * Currently, TypeProf provides only the analysis engine and a minimal set of features.
  * The analysis result would be useful not only to generate RBS prototype
    but also identifying the source location of a method definition, listing callsites of a method,
    searching a method call by its argument types, etc.
  * Sometimes, TypeProf prints very big union type, such as `Integer | Float | Complex | Rational | ...`.
    Worse, the same big type is printed multiple times.
    It may be useful to factor out such a long type by using type alias, for example.
