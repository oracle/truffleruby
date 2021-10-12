# Testing TruffleRuby

## Types of tests

We talk about several types of tests in TruffleRuby:

* Unit tests (`UNIT`)
* TCK (`TCK`)
* Specs (`SPECS`)
* MRI tests (`MRI`)
* MRI basictest (`BASIC`)
* MRI bootstraptest (`BOOT`)
* C extension tests (`CEXT`)
* Bundler tests (`BUN`)
* Compiler tests (`COMP`)
* Ecosystem tests (`ECO`)
* Gem tests (`GEM`)
* Integration tests (`IGR`)
* Docker tests
* Module tester

### Unit tests

Java unit tests test parts of the internal Java implementation which are hard
to test at the Ruby level.

Java unit tests live in `src/test/java/org/truffleruby`.

To run Java unit tests run `jt test unittest`.

### TCK

The TCK is a parametric test suite provided by GraalVM that tests language
interoperability.

The TCK Ruby parameters are in
`src/test/java/org/truffleruby/RubyTCKLanguageProvider.java`.

Run the TCK with `jt test tck` or `mx tck`.

### Specs

By *specs* we mean the `ruby/spec` (formerly RubySpec) suite, and additional
specs we've written for TruffleRuby-specific behaviour using the same MSpec
tool.

We usually think of the specs for the C extension API as being separate
(`SPECS(CEXT)`).

Specs are in `spec/`.

Run specs with `jt test specs`, or `ruby spec/mspec/bin/mspec`.

### MRI tests

The MRI tests use the `test/unit` library. We find them a bit more difficult to
run and configure, and find that they often have a lower granularity, so they
aren't used as frequently as the specs.

We usually think of the specs for the C extension API, and MRI tests that use C
extensions, as being separate (`MRI(CEXT)`), and these are run with different
flags like `--syslog`.

MRI tests are in `test/mri`.

Run MRI tests with `jt test mri`.

### MRI basictest

MRI's basictest is a smaller set of tests for some basic control structures
and language features. It is in `test/basictest`.

Run basictest with `jt test basictest`.

### MRI bootstraptest

MRI's bootstraptest is a smaller set of tests for functionality they require
to bootstrap their implementation, including some tests against regressions
and corner cases. It is in `test/bootstraptest`.

Run bootstraptest with `jt test bootstraptest`. It's not
tractable to run bootstraptest with the JVM, as it starts a new Ruby process
for each test. Run it with a native build instead.

### C extension tests

C extension tests are a basic test of compiling, loading and running C
extensions. They don't test individual C extension methods, which are instead
done by the C extension API specs and MRI tests.

There are also tests for building and using some basic real-world C extension
gems. You will need something called the *gem test pack* to run these which,
because we do not want to distribute gems, is not publicly available.

C extension tests are in `test/truffle/cexts`.

Run C extension tests with `jt test cexts`.

### Bundle tests

The Bundle tests check that Bundler can be installed, it can be used to
`bundle install` a code base, and `bundle exec` it.

Run the Bundler tests with `jt test bundle`.

### Compiler tests

Compiler tests check that the compiler is working as we expect. It is difficult
to test a compiler is producing output as expected without examining either
intermediate representation graphs or produced machine code, so for all our
compiler tests we use the simpler test of checking that a value compiles to a
constant. We find that this is a good pass-or-fail test that the compiler is
working as intended.

The compiler tests include a sort of test suite for compilation, called the
partial evaluation (*PE*) tests, as well as tests for more subtle things like
on-stack-replacement.

Special methods such as `Primitive.assert_compilation_constant` are used to implement
this.

Compiler tests are in `test/truffle/compiler`.

Run compiler tests with `jt test compiler` or by manually running the
scripts.

### Ecosystem tests

Ecosystem tests test applications of key Ruby projects such as Rails.

Ecosystem tests are in `test/truffle/ecosystem`.

Run ecosystem tests with `jt test ecosystem`.

### Gem tests

Gem tests test a few gems that exercise particular behaviour that we think isn't
exercised well by other tests.

We usually think of gem tests for C extension gems as being separate
(`GEM(CEXT)`).

Ecosystem tests are in `test/truffle/gems`.

Run ecosystem tests with `jt test gems`. You will need something called
the *gem test pack* which, because we do not want to distribute gems, is not
publicly available.

### Integration tests

Integration tests test things that are too cumbersome to test easily in specs,
often because the test the launcher or interpreter rather than the language, or
they involve multiple processes, such as interpreter options, logging, error
output, servers, finalisers, coverage, and so on.

Integration tests are in `test/truffle/integration`.

Run integration tests with `jt test integration` or by manually running the
scripts.

### Docker tests

The Docker tests use Docker as a virtualisation and software configuration
tool to test installing and using TruffleRuby in different Linux distributions,
installing from different sources, and in other types of configuration.

The Docker basic tests run:

* Installing from rubygems.org and minimally using functionality from `color`,
  `oily_png`, `unf`

The Docker full tests additionally run:

* `SPEC` and `SPEC(CEXT)`
* `COMP(PE)`

Docker tests always run with the compiler enabled.

See the [docker documentation](docker.md) for more details.

### Module tester

The module tester uses builds of TruffleRuby to run the test or spec suites of
third-party gems as a final level of testing.

The module tester is not public, but results can be seen at
http://www.graalvm.org/docs/reference-manual/compatibility/.

## Configurations

There is a hyperspace of combinations of tests and different configuration
options. It's hard to document what we do test from this space, as we can't draw
an n-dimension table. The table inevitably has a lot of combinations
and configurations that aren't tested, due to limited resources.

* Java 8 `J8` and 11 `J11`
* Interpreter `INT`
* Graal CE and EE
* SVM CE and EE
* Linux and macOS
* Different distributions of Linux

### Tests run in CI

* `UNIT` with `INT` on `J8` on Linux.
* `UNIT` with `INT` on `J8` on macOS.
* `BASICTEST`, `SPEC` with `INT` on `J8` on Linux.
* `BASICTEST`, `SPEC` with `INT` on `J8` on macOS.
* `SPEC(FAST)` with `INT` on `J11` on Linux.
* `TCK` with `INT` on `J8` on Linux.
* `MRI` with `INT` on `J8` on Linux.
* `MRI` with `INT` on `J8` on macOS.
* `IGR` with `INT` on `J8` on Linux.
* `CEXT`, `SPEC(CEXTS)`, `MRI(CEXTS)`, `BUN` with `INT` on `J8` on Linux (the Sulong downstream gate).
* `CEXT`, `SPEC(CEXTS)`, `MRI(CEXTS)`, `BUN` with `INT` on `J8` on macOS (the Sulong downstream gate).
* `GEM` with `INT` on `J8` on Linux.
* `ECO` with `INT` on `J8` on Linux.
* `COMP` with Graal CE on `J8` on Linux.
* `SPEC` with SVM CE and EE on Linux and macOS.
* `SPEC` and `SPEC(CEXT)` on Linux in the standalone distribution configuration.

### Tests run on release candidates

* Docker full tests on all supported Linux distributions, on all supported Ruby
  version managers, using:
  * release candidate CE tarball and Ruby component,
  * release candidate EE tarball and Ruby component
  * the standalone Ruby tarball.
* macOS manually running similar tests.

## Un-used tests and configurations

We have additional tests, configurations and dimensions we'd like to add to
testing, but we don't use at the moment, including:

* Running tests with the compiler enabled.
* Running tests with the compiler enabled, background-compilation disabled
  and compile-immediately (the *stress* option).
* Running tests in a loop.
* Running tests in a random order.
* Running tests with additional logic to randomise hidden internal interpreter
  state such as array strategies.
* Checking the coverage of our tests.
