# The TruffleRuby Contributor Workflow

## Requirements

You will need:

* Java JDK 8 or 9
* Ruby 2
* [LLVM](../user/installing-llvm.md)
* [`libssl`](../user/installing-libssl.md)

You can remove the dependencies on LLVM and `libssl` by setting
`export TRUFFLERUBY_CEXT_ENABLED=false`.

## Workspace directory

We recommend creating an extra directory for building TruffleRuby:

```bash
$ mkdir truffleruby-ws
$ cd truffleruby-ws
```

## Developer tool

We then use a Ruby script to run most commands.

```bash
$ git clone https://github.com/oracle/truffleruby.git
$ cd truffleruby
$ ruby tool/jt.rb --help
```

Most of us add a function to our shell profile file so that it can be run with
just `jt`. To allow this to run from any path, add this to your `.bash_profile`:
```bash
$ echo 'function jt { ruby '$PWD'/tool/jt.rb "$@"; }' >> ~/.bash_profile
```

```bash
$ jt --help
```

## Installing `mx`

`mx` is the Python build tool used to build Graal-related projects.
You can install it automatically with:

```bash
jt mx version
```

## Building

We recommend configuring the build to use the Truffle framework as a binary
dependency rather than importing it as source code.

```bash
$ echo MX_BINARY_SUITES=truffle,sdk > mx.truffleruby/env
```

```bash
$ jt build
```

By default the `jt build` command only builds the JVM-based launcher for
TruffleRuby. If you'd like to build the native image using the Substrate VM, you
can do so by providing an extra argument to the build command.

```bash
$ jt build native
```

This will create a new executable named `native-ruby` in the `bin/` directory.

Note that build information such as the date and Git revision hash will not be
updated when you build for a second time. Releases should always be built from
scratch.

## Sulong

TruffleRuby runs C extension using Sulong. You should build Sulong from source.
You can clone Sulong and build it with:

```bash
jt build sulong
```

## Testing

We have 'specs' which come from the Ruby Spec Suite. These are usually high
quality, small tests, and are our priority at the moment. We also have MRI's
unit tests, which are often very complex and we aren't actively working on now.
Finally, we have tests of our own. The integration tests test more macro use of
Ruby. The ecosystem tests test commands related to Ruby. The gems tests test a
small number of key Ruby 3rd party modules.

The basic test to run every time you make changes is a subset of specs which
runs in reasonable time. If you are running a Ruby environment manager like
`rvm`, `rbenv`, or `chruby` please run `rvm use system`, `rbenv system`,
or `chruby system` to clear their environment variables.

```bash
$ jt test fast
```

You may also want to regularly run the integration tests.

```bash
$ jt test integration
```

Other tests can be hard to set up and can require other repositories, so we
don't normally run them locally unless we're working on that functionality.

If you'd like to run tests with the native TruffleRuby binary, you can do so
by providing the `--native` argument to the test command. Please note that
you must follow the steps to build the native image before it can be used 
for tests.

```bash
$ jt test --native fast
```

## Running

`jt ruby` runs TruffleRuby. You can use it exactly as you'd run the MRI `ruby`
command. Although it does set a couple of extra options to help you when
developing, such as loading the core library from disk rather than the JAR.
`jt ruby` prints the real command it's running as it starts.

```bash
$ bin/ruby ...
$ jt ruby ...
```

If you'd like to run the native TruffleRuby binary, you can do so
by providing the `--native` argument to the `ruby` command. Please note that
you must follow the steps to build the native image before it can be used.

```bash
$ bin/native-ruby ...
$ jt ruby --native ...
```

## Options

Specify JVM options with `-J-option`.

```bash
$ jt ruby -J-Xmx1G test.rb
```

TruffleRuby options are set with `-Xname=value`. For example
`-Xexceptions.print_java=true` to print Java exceptions before translating them
to Ruby exceptions. You can leave off the value to set the option to `true`.

To see all options run `jt ruby -Xoptions`.

You can also set JVM options in the `JAVA_OPTS` environment variable (don't
prefix with `-J`) variable. Ruby command line options and arguments can also be
set in `RUBYOPT`.

## Running with Graal

To run with a GraalVM binary tarball, set the `GRAALVM_BIN` environment variable
and run with the `--graal` option. Note that if you're running a native TruffleRuby
binary, Graal is always built into the binary and enabled; you can safely ignore
the rest of this section.

```bash
$ export GRAALVM_BIN=.../graalvm-0.nn/bin/java
$ jt ruby --graal ...
```

You can check this is working by printing the value of `Truffle.graal?`.

```bash
$ export GRAALVM_BIN=.../graalvm-0.nn/bin/java
$ jt ruby --graal -e 'p Truffle.graal?'
```

To run with Graal built from source, set `GRAAL_HOME`. Or it will be
automatically found if it is cloned into the same directory as `truffleruby`, it
is built, and you are not using a binary suite for Truffle.

```bash
$ export GRAAL_HOME=.../graal
$ jt ruby --graal ...
```

Set Graal options as any other JVM option.

```bash
$ jt ruby --graal -J-Dgraal.TraceTruffleCompilation=true ...
```

We have flags in `jt` to set some options, such as `--trace` for
`-J-Dgraal.TraceTruffleCompilation=true` and `--igv` for
`-J-Dgraal.Dump=Truffle`.

## Testing with Graal

The basic test for Graal is to run our compiler tests. This includes tests that
things partially evaluate as we expect, that things optimise as we'd expect,
that on-stack-replacement works and so on.

```bash
$ jt test compiler
```

## Using Docker

Docker can be a useful tool for creating isolated environments for development
and testing. Dockerfiles can also serve as executable documentation of how to
set up a development environment. See `test/truffle/docker` and `tool/docker`.

For end-users, see `../user/docker.md`.

## How to fix a failing spec

We usually use the `jt untag` command to work on failing specs. It runs only
specs that are marked as failing.

```bash
$ jt untag spec/ruby/core/string
```

When you find a spec that you want to work on, it can be easier to look at the
spec's source (for example look in `spec/ruby/core/string`) and recreate it
as a standalone Ruby file for simplicity.

Then you probably want to run with `-Xexceptions.print_java` if you see a Java
exception.

When the spec is fixed the `untag` command will remove the tag and you can
commit the fix and the removal of the tag.

## Running specs for Ruby 2.4/2.5 features

TruffleRuby currently targets Ruby 2.3. However, we welcome pull requests for
Ruby 2.4/2.5 features as long as they don't conflict significantly with
Ruby 2.3 semantics.

It is possible to run specs for Ruby 2.4 and 2.5 features by setting
`PRETEND_RUBY_VERSION`:

```bash
# String#match? is introduced in 2.4
$ PRETEND_RUBY_VERSION=2.4.3 jt test spec/ruby/core/string/match_spec.rb
# Integer#digits is introduced in 2.5
$ PRETEND_RUBY_VERSION=2.5.0 jt test spec/ruby/core/integer/digits_spec.rb
```

This also works for `jt tag`/`jt untag`.

When working on a 2.4/2.5 feature, add the spec file in the corresponding file
list (`:ruby24` or `:ruby25`) in `spec/truffle.mspec` so that the specs are run
in the CI too.

## How to fix a failing MRI test

Remove the exclusion of either the file (`test/mri_standard.exclude`) or the
individual method (`test/mri/excludes_truffle`) and run the individual file
`jt test mri test/mri/file.rb` to see any errors.

As with specs, you probably then want to recreate the test in a standalone Ruby
file to fix it.

You can also recompute the tags automatically for an entire test file with
`jt retag test/mri/file.rb`.
