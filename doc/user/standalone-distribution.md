# The Standalone Distribution

There are three ways to install TruffleRuby, see
[getting started](../../README.md#getting-started). The recommended way is
[GraalVM](installing-graalvm.md). You can also use what we call the standalone
distribution of TruffleRuby, either via your Ruby manager/installer, or as a
simple binary tarball.

Releases of the standalone distribution are
[available on GitHub](https://github.com/oracle/truffleruby/releases).

The standalone distributions are the files:

```
truffleruby-VERSION-linux-amd64.tar.gz
truffleruby-VERSION-macos-amd64.tar.gz
```

## Testing TruffleRuby in TravisCI

TruffleRuby is now integrated in TravisCI.
Just add `truffleruby` in the build matrix, such as:

```yaml
language: ruby
rvm:
  - 2.6.0
  - truffleruby
```

See https://docs.travis-ci.com/user/languages/ruby#truffleruby for details.
Please [report](https://github.com/oracle/truffleruby/issues) any issue you might find while testing with TruffleRuby.

## Testing TruffleRuby in CI

If you use another continuous integration system, you can follow these
instructions to run TruffleRuby in CI.

In short, one only needs to download and extract the archive, add it to `PATH`
and run the post-install script.

Set `TRUFFLERUBY_VERSION` to the latest TruffleRuby version from
[GitHub releases](https://github.com/oracle/truffleruby/releases).

```bash
export TRUFFLERUBY_VERSION=<desired_version>
export TRUFFLERUBY_RESILIENT_GEM_HOME=true
curl -L https://github.com/oracle/truffleruby/releases/download/vm-$TRUFFLERUBY_VERSION/truffleruby-$TRUFFLERUBY_VERSION-linux-amd64.tar.gz | tar xz
export PATH="$PWD/truffleruby-$TRUFFLERUBY_VERSION-linux-amd64/bin:$PATH"
$PWD/truffleruby-$TRUFFLERUBY_VERSION-linux-amd64/lib/truffle/post_install_hook.sh
ruby -v # => truffleruby 1.0.0-rc3, like ruby 2.4.4, GraalVM CE Native [x86_64-linux]
```

Note that you also need to ensure `GEM_HOME` and `GEM_PATH` are not set, so
TruffleRuby uses the correct `GEM_HOME` and `GEM_PATH`. This is the reason for
`export TRUFFLERUBY_RESILIENT_GEM_HOME=true` above.
See [Using TruffleRuby without a Ruby manager](ruby-managers.md#using-truffleruby-without-a-ruby-manager)
for details.

## Dependencies

[TruffleRuby's dependencies](../../README.md#dependencies) need to be installed
for TruffleRuby to run correctly.
