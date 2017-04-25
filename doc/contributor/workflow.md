# The TruffleRuby Contributor Workflow

## Requirements

You will need:

* Java 8 (not 9 EA)
* Ruby 2

## Developer tool

We use a Ruby script to run most commands.

```
$ ruby tool/jt.rb --help
```

Most of us create a symlink to this executable somewhere on our `$PATH` so
that we can simply run.

```
$ jt --help
```

## Building

```
$ jt build
```

## Testing

We have 'specs' which come from the Ruby Spec Suite. These are usually high
quality, small tests, and are our priority at the moment. We also have MRI's
unit tests, which are often very complex and we aren't actively working on now.
Finally, we have tests of our own. The integration tests test more macro use of
Ruby. The ecosystem tests test commands related to Ruby. The gems tests test a
small number of key Ruby 3rd party modules.

The basic test to run every time you make changes is a subset of specs which
runs in reasonable time.

```
$ jt test fast
```

You may also want to regularly run the integration tests.

```
$ jt test integration
```

Other tests can be hard to set up and can require other repositories, so we
don't normally run them locally unless we're working on that functionality.

## Running

`jt ruby` runs TruffleRuby. You can use it exactly as you'd run the MRI `ruby`
command. Although it does set a couple of extra options to help you when
developing, such as loading the core lirbary from disk rather than the JAR. `jt
ruby` prints the real command it's running as it starts.

```
$ ruby ...
$ jt ruby ...
```

## Options

Specify JVM options with `-J-option`.

```
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
and run with the `--graal` option.

```
$ export GRAALVM_BIN=.../graalvm-0.nn/bin/java
$ jt ruby --graal ...
```

You can check this is working by printing the value of `Truffle::Graal.graal?`.

```
$ export GRAALVM_BIN=.../graalvm-0.nn/bin/java
$ jt ruby --graal -e 'p Truffle::Graal.graal?'
```

To run with Graal built from source, set `GRAAL_HOME`.

```
$ export GRAAL_HOME=.../graal-core
$ jt ruby --graal ...
```

Set Graal options as any other JVM option.

```
$ jt ruby --graal -J-Dgraal.TraceTruffleCompilation=true ...
```

We have flags in `jt` to set some options, such as `--trace` for
`-J-Dgraal.TraceTruffleCompilation=true` and `--igv` for
`-J-Dgraal.Dump=Truffle`.

## Testing with Graal

The basic test for Graal is to run our compiler tests. This includes tests that
things partially evaluate as we expect, that things optimise as we'd expect,
that on-stack-replacement works and so on.

```
$ jt test compiler
```

## mx and integrating with other Graal projects

TruffleRuby can also be built and run using `mx`, like the other Graal projects.
This is intended for special cases such as integrating with other Graal
projects, and we wouldn't recommend using it for normal development. If you do
use it, you should clean before using `jt` again as having built it with `mx`
will change some behaviour.

## How to fix a failing spec

We usually use the `jt untag` command to work on failing specs. It runs only
specs that are marked as failing.

```
$ jt untag spec/ruby/core/string
```

When you find a spec that you want to work on it's usually best to look at the
spec's source (for example look in `spec/ruby/core/string`) and recreate it
as a standalone Ruby file for simplicity.

Then you probably want to run with `-Xexceptions.print_java` if you see a Java
exception.

When the spec is fixed the `untag` command will remove the tag and you can
commit the fix and the removal of the tag.

## How to fix a failing MRI test

Remove the exclusion of either the file (`test/mri_standard.exclude`) or the the
individual method (`test/mri/excludes_truffle`) and run the individual file
`jt test mri test/mri/file.rb` to see any errors.

As with specs, you probably then want to recreate the test in a standalone Ruby
file to fix it.
