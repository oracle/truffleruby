# The Standalone Distribution

There are three ways to install TruffleRuby, see
[getting started](../../README.md#getting-started). The first way is
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

## Testing TruffleRuby in CI

The standalone distributions are also useful for testing, for instance to test
TruffleRuby in your continuous integration system. If you use TravisCI,
[see below](#testing-truffleruby-in-travisci).
If you use another continuous integration system, you can follow these
instructions to run TruffleRuby in CI.

In short, one only needs to download and extract the archive, add it to `PATH`
and run the post-install script:

```bash
export TRUFFLERUBY_VERSION=1.0.0-rc3
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

## Testing TruffleRuby in TravisCI

For TravisCI, we
[plan to integrate](https://github.com/travis-ci/travis-ci/issues/9803)
directly so one could just use `rvm: truffleruby` in the build matrix.
However, this is not complete yet. In the meantime, the following example
configuration can be used:

```yaml
script:
  - bundle exec rspec
matrix:
  include:
  - rvm: 2.5.1
  - rvm: system
    install:
      - export TRUFFLERUBY_VERSION=1.0.0-rc3
      - curl -L https://github.com/oracle/truffleruby/releases/download/vm-$TRUFFLERUBY_VERSION/truffleruby-$TRUFFLERUBY_VERSION-linux-amd64.tar.gz | tar xz
      - export PATH="$PWD/truffleruby-$TRUFFLERUBY_VERSION-linux-amd64/bin:$PATH"
      - gem install bundler
      - bundle install
```

Running the post-install script is currently not necessary on TravisCI for
TruffleRuby 1.0.0-rc3 or more recent, so this step is skipped here.

## Dependencies

[TruffleRuby's dependencies](../../README.md#dependencies) need to be installed
for TruffleRuby to run correctly.
