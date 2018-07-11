# Testing TruffleRuby

## Types of tests

We talk about several types of tests in TruffleRuby:

* Unit tests (`UNIT`)
* TCK (`TCK`)
* Specs (`SPECS`)
* MRI tests (`MRI`)
* C extension tests (`CEXT`)
* Compiler tests (`COMP`)
* Ecosystem tests (`ECO`)
* Gem tests (`GEM`)
* Integration tests (`IGR`)
* Docker tests
* Module tester

### Unit tests

Java unit tests test 

### TCK

The TCK is a parametric test suite provided by GraalVM that tests language
interoperability.

### Specs

By *specs* we mean the `ruby/spec` (formerly RubySpec) suite, and additional
specs we've written for TruffleRuby-specific behaviour using the same MSpec
tool.

We usually think of the specs for the C extension API as being separate
(`SPECS(CEXT)`).

Specs are in `spec/`.

Run specs with `jt test spec`, or
`ruby spec/mspec/bin/mspec --config spec/truffle.mspec`.

### MRI tests

The MRI tests use the `test/unit` library. We find them a bit more difficult to
run and configure, and find that they often have a lower granularity, so they
aren't used as frequently as the specs.

We usually think of the specs for the C extension API as being separate
(`MRI(CEXT)`).

MRI tests are in `test/mri`.

Run MRI tests with `jt test mri`, or
`ruby test/mri/tests/runner.rb test-files...`.

### C extension tests

C extension tests are a basic test of compiling, loading and running C
extensions. They don't test individual C extension methods, which are instead
done by the C extension API specs and MRI tests.

There are also tests for building and using some basic real-world C extension
gems. You will need something called the *gem test pack* to run these which,
because we do not want to distribute gems, is not publicly available.

C extension tests are in `test/truffle/cexts`.

Run C extension tests with `jt test cexts`.

### Compiler tests

Compiler tests check that the compiler is working as we expect. It is difficult
to test a compiler is producing output as expected without examining either
intermediate representation graphs or produced machine code, so for all our
compiler tests we use the simpler test of checking that a value compiles to a
constant. We find that this is a good pass-or-fail test that the compiler is
working as intended.

The compiler tests include a sort of specs suite for compilation, called the
partial evaluation (*PE*) tests, as well as tests for more subtle things like
on-stack-replacement.

Special methods such as `Truffle::Graal.assert_constant` are used to implement
this.

Compiler tests are in `test/truffle/compiler`.

Run compiler tests with `jt test compiler` or by manually running the
scripts.

### Ecosystem tests

Ecosystem tests test applications of capstone Ruby projects such as Rails.

Ecosystem tests are in `test/truffle/ecosystem`.

Run ecosystem tests with `jt test ecosystem`. You will need something called
the *gem test pack* which, because we do not want to distribute gems, is not
publicly available.

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

The Docker tests using Docker as a virtualisation and software configuration
tool to test installing and using TruffleRuby in different Linux distributions,
installing from different sources, using different Ruby version managers,
rebuilding images or not, and in other types of configuration.

The Docker basic tests run:

* Installing from rubygems.org and minimally using functionality from `color`,
  `oily_png`, `unf`

The Docker full tests additionally run:

* `SPEC` and `SPEC(CEXT)`
* `CEXT(PE)`

Docker tests always run with the compiler enabled.

See the [docker documentation](docker.md) for more details.

### Module tester

The module tester uses builds of TruffleRuby to run the test or spec suites of
third-party gems as a final level of testing.

The module tester is not public, but results can be seen at
http://www.graalvm.org/docs/reference-manual/compatibility/.

## Configurations

There is a hyperspace of combinations of tests and different configuration
options, which is hard to document and inevitably has some holes in what we have
resources to test.

* Java 8 `J8` and 11 `J11`
* Interpreter `INT`
* Graal CE and EE
* SVM CE and EE
* Linux and macOS
* Different distributions of Linux

### Tests run in CI

* `SPEC` with `INT` on `J8` on Linux.
* `SPEC` with `INT` on `J8` on macOS.
* `SPEC(FAST)` with `INT` on `J11` on Linux.
* `TCK` with `INT` on `J8` on Linux.
* `MRI` with `INT` on `J8` on Linux.
* `IGR` with `INT` on `J8` on Linux.
* `CEXT` with `INT` on `J8` on Linux.
* `GEM` with `INT` on `J8` on Linux.
* `ECO` with `INT` on `J8` on Linux.
* `COMP` with Graal CE on `J8` on Linux.
* `MRI` with `INT` on `J8` on macOS.
* `COMP` with Graal on `J8` on macOS.
* `build,ruby_debug,ruby_product` with SVM CE and EE on Linux.
* `build,darwin_ruby` with SVM CE and EE on macOS.

### Tests run on release candidates

* Docker full tests on all supported Linux distributions, on all supported Ruby
  version managers, using release candidate CE and EE tarball and Ruby
  component, and rebuilding images or not.

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
