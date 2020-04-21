# The TruffleRuby Contributor Workflow

## Requirements

See the [Dependencies in the README](../../README.md#dependencies).
Additionally, you will need:

* Ruby >= 2.4

The requirements include a [C compiler](../user/installing-llvm.md). Because it's a common issue, we remind macOS users
they might need to add `export SDKROOT=$(xcrun --show-sdk-path)` to their shell profile.

## Workspace directory

We recommend creating an extra directory for building TruffleRuby:

```bash
$ mkdir truffleruby-ws
$ cd truffleruby-ws
```

You can then clone the repository:
```bash
$ git clone https://github.com/oracle/truffleruby.git
$ cd truffleruby
```

## Developer tool

We then use a Ruby script to run most commands.

```bash
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

## Building

```bash
$ jt build
```

By default the `jt build` command builds a small JVM-only (no native images)
GraalVM containing only the Ruby language. The built GraalVM can be found in the
`mxbuild/truffleruby-jvm` directory.

There are multiple *configurations* available to build TruffleRuby:
* `jvm`: the default, JVM-only, no GraalVM compiler
* `jvm-ce`: JVM-only, with the GraalVM compiler
* `native`: Builds a native image of TruffleRuby using SubstrateVM

To build one of these configurations, pass `--env` to the build command:
```bash
$ jt build [--env CONFIGURATION]
```

The GraalVM build will be created in `mxbuild/truffleruby-${configuration}`.

Note that build information such as the date and Git revision hash will not be
updated when you build for a second time. Releases should always be built from
scratch.

### Building C Extensions Faster

To speed up compilation of bundled C extensions, it is possible to use
*native* toolchain launchers, which might save some build time.
See the [related documentation](https://github.com/oracle/graal/blob/master/sulong/docs/TOOLCHAIN.md#using-a-prebuilt-graalvm-as-a-bootstrapping-toolchain)
in Sulong to build and use them.

You can also use `export JT_CACHE_TOOLCHAIN=true` to have the native toolchain
launchers built and used by `jt` automatically. `jt` will keep the 4 newest
built toolchain launchers to avoid rebuilding when branches are switched.

## Running

`jt ruby` runs TruffleRuby. You can use it exactly as you'd run the MRI `ruby`
command. Additionally, `jt ruby` sets a couple of extra options to help you when
developing, such as loading the core library from disk rather than the JAR.
`jt ruby` prints the real command it's running as it starts.

If you are running a Ruby environment manager like `rvm`, `rbenv`, or `chruby`
please run `rvm use system`, `rbenv system`, or `chruby system` to clear their
environment variables, so that the correct gems are picked up.

If you want to run the TruffleRuby with another configuration than `jvm`, pass
the configuration name after `--use`:
```bash
$ jt [--use CONFIGURATION] ruby ...
```

## Testing

We have 'specs' which come from [the Ruby Spec Suite](https://github.com/ruby/spec).
These are usually high quality, small tests, and are our priority at the moment.
We also have MRI's unit tests, which are often very complex and we aren't
actively working on now. Finally, we have tests of our own. The integration
tests test more macro use of Ruby. The ecosystem tests test commands related to
Ruby. The gems tests test a small number of key Ruby 3rd party modules.

The basic test to run every time you make changes is the "fast specs", a subset
of specs which runs in reasonable time.

```bash
$ jt [--use CONFIGURATION] test fast
```

Other tests take longer and require more setup, so we don't normally run them
locally unless we're working on that functionality (instead, the CI runs them).

Specs under `spec/ruby` are supposed to pass on both Truffle and MRI.
To run the tests or specs on MRI, pass `--use ruby`:
```bash
jt --use ruby test path/to/spec.rb # assumes you have MRI in your PATH
jt --use /full/path/to/bin/ruby path/to/spec.rb test
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

Ruby command line options and arguments can also be set in `RUBYOPT` or
`TRUFFLERUBYOPT`.

## Running with Graal

To build TruffleRuby with the GraalVM CE compiler, use:
```bash
$ jt build --env jvm-ce
```

Then, run TruffleRuby with:
```bash
$ jt --use jvm-ce ruby ...
```

We have flags in `jt` to set some options, such as `--trace` for
`--engine.TraceCompilation`.

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

## Running specs for Ruby 2.7 features

TruffleRuby currently targets Ruby 2.6. However, we welcome pull requests for
Ruby 2.7 features as long as they don't conflict significantly with
Ruby 2.6 semantics.

It is possible to run specs for Ruby 2.7 features by setting
`PRETEND_RUBY_VERSION`:

```bash
# File.absolute_path? is introduced in 2.7
$ PRETEND_RUBY_VERSION=2.7.0 jt test spec/ruby/core/file/absolute_path_spec.rb
```

This also works for `jt tag`/`jt untag`.

When working on a feature from the next version of Ruby, add the spec file in
the corresponding file list (`:next`) in `spec/truffleruby.mspec` so that the
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
