# Distributions

In general, it is preferable to [use GraalVM](using-graalvm.md)
for running TruffleRuby. GraalVM also includes other languages like JavaScript,
R and Python. However, GraalVM currently requires a click-through license and
is distributed under the OTN license.

Distributions of TruffleRuby with or without Sulong
and with or without Graal are also made available on the
[Releases page](https://github.com/oracle/truffleruby/releases).

Currently, these distributions are limited to Linux as we do not know a way to
get OpenJDK 8 binaries for other platforms on which to build the JVMCI support.

To use such a distribution, unpack it and add `truffleruby/bin` to your `$PATH`:

```bash
$ tar xf truffleruby-....tar.gz
$ export PATH="$PWD/truffleruby/bin:$PATH"
$ ruby -v
# => truffleruby
```

You can also download and extract in a single step (useful in CI):

```bash
# Download and extract in folder truffleruby/
$ curl -L https://github.com/oracle/truffleruby/releases/....tar.gz | tar xz
$ export PATH="$PWD/truffleruby/bin:$PATH"
$ ruby -v
# => truffleruby
```

The minimal distribution only needs JRE 8, curl and bash.
