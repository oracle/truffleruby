# The TruffleRuby Contributor Workflow

## Requirements

You will need:

* Java JDK 8
* Ruby 2
* [LLVM](../user/installing-llvm.md)
* [`libssl`](../user/installing-libssl.md)

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

Most of us add an alias to our shell profile file so that it can be run with
just `jt`. To allow this to run from any path, add this to your `.bash_profile`:

```bash
$ echo 'alias jt=/path/to/mri/bin/ruby /path/to/truffleruby/tool/jt.rb' >> ~/.bash_profile
```

```bash
$ jt --help
```

## Installing `mx`

`mx` is the Python build tool used to build Graal-related projects.
You can install it automatically with:

```bash
$ jt mx version
```

## Building

```bash
$ jt build
```

By default the `jt build` command builds GraalVM containing only the Ruby
language. The built GraalVM can be found in `mxbuild/truffleruby-jvm` directory.
If you'd like to build GraalVM with the native image using the Substrate VM, you
can do so by providing an extra option `--env` to the build command.

```bash
$ jt build --env native
```

This GraalVM build will be created in `mxbuild/truffleruby-native`.

Note that build information such as the date and Git revision hash will not be
updated when you build for a second time. Releases should always be built from
scratch.

## Testing

We have 'specs' which come from the Ruby Spec Suite. These are usually high
quality, small tests, and are our priority at the moment. We also have MRI's
unit tests, which are often very complex and we aren't actively working on now.
Finally, we have tests of our own. The integration tests test more macro use of
Ruby. The ecosystem tests test commands related to Ruby. The gems tests test a
small number of key Ruby 3rd party modules.

The basic test to run every time you make changes is the "fast specs", a subset
of specs which runs in reasonable time. If you are running a Ruby environment
manager like `rvm`, `rbenv`, or `chruby` please run `rvm use system`, `rbenv
system`, or `chruby system` to clear their environment variables.

```bash
$ jt test fast
```

Other tests take longer and require more setup, so we don't normally run them
locally unless we're working on that functionality (instead, the CI runs them).

If you'd like to run tests with the native TruffleRuby binary, you can do so by
providing the `--use` option. Please note that you must follow the steps to
build the native image before it can be used for tests.

```bash
$ jt --use native test fast
```

Tests under `spec/ruby` are supposed to pass on both Truffle and MRI. Use `jt
--use mri/bin/ruby path/to/spec.rb` to run the test on MRI, assuming you have
MRI in your PATH.

## Running

`jt ruby` runs TruffleRuby. You can use it exactly as you'd run the MRI `ruby`
command. Although it does set a couple of extra options to help you when
developing, such as loading the core library from disk rather than the JAR.
`jt ruby` prints the real command it's running as it starts.

If you'd like to run the native TruffleRuby binary, you can do so by providing
the `--use` option. Please note that you must follow the steps to build the
native image before it can be used.

```bash
$ jt --use native ruby ...
```

## Options

Specify JVM options with `--vm.option`.

```bash
$ jt ruby --vm.Xmx1G test.rb
```

TruffleRuby options are set with `--name=value`. For example
`--exceptions-print-java=true` to print Java exceptions before translating them
to Ruby exceptions. You can leave off the value to set the option to `true`.

To see all options run `jt ruby --help:languages`.

The development launcher supports `RUBY_BIN`, `JAVACMD`, `JAVA_HOME`, and
`JAVA_OPTS`.

Ruby command line options and arguments can also be set in `RUBYOPT`.

## Running with Graal

See the document on [building graal](building-graal.md), and then run `jt ruby`
as normal.

We have flags in `jt` to set some options, such as `--trace` for
`--vm.Dgraal.TraceTruffleCompilation=true` and `--igv` for
`--vm.Dgraal.Dump=Truffle`.

## Testing with Graal

The basic test for Graal is to run our compiler tests. This includes tests that
things partially evaluate as we expect, that things optimise as we'd expect,
that on-stack-replacement works and so on.

```bash
$ jt test compiler
```

## How to fix a failing spec

We usually use the `jt untag` command to work on failing specs. It runs only
specs that are marked as failing.

```bash
$ jt untag spec/ruby/core/string
```

When you find a spec that you want to work on, it can be easier to look at the
spec's source (for example look in `spec/ruby/core/string`) and recreate it
as a standalone Ruby file for simplicity.

Then you probably want to run with `--exceptions-print-java` if you see a Java
exception.

When the spec is fixed the `untag` command will remove the tag and you can
commit the fix and the removal of the tag.

## Running specs for Ruby 2.5 features

TruffleRuby currently targets Ruby 2.4. However, we welcome pull requests for
Ruby 2.5 features as long as they don't conflict significantly with
Ruby 2.4 semantics.

It is possible to run specs for Ruby 2.5 features by setting
`PRETEND_RUBY_VERSION`:

```bash
# Integer#digits is introduced in 2.5
$ PRETEND_RUBY_VERSION=2.5.0 jt test spec/ruby/core/integer/digits_spec.rb
```

This also works for `jt tag`/`jt untag`.

When working on a feature from the next version of Ruby, add the spec file in
the corresponding file list (`:next`) in `spec/truffle.mspec` so that the
specs are run in CI too.

## How to fix a failing MRI test

Remove the exclusion of either the file (`test/mri/failing.exclude`) or the
individual method (`test/mri/excludes/...`) and run the individual file
`jt test mri test/mri/tests/file.rb` to see any errors.

As with specs, you probably then want to recreate the test in a standalone Ruby
file to fix it.

You can also recompute the tags automatically for an entire test file with
`jt retag test/mri/tests/file.rb`.

## Building the parser

TruffleRuby uses the Jay parser generator. A copy of this is located in
`tool/jay`. The command `jt build parser` will build Jay, if needed, and then
regenerate the parser. We check the generated parser into the source repository.
