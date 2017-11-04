# Creating a TruffleRuby distribution

See the [Distributions page](../user/using-distribution.md).

The tool to build these distributions is `tool/make-distribution.sh`.

This script should be run in a fresh checkout of truffleruby in a new workspace.
This is necessary as otherwise extra gems and build artifacts might be included.

```bash
$ mkdir truffleruby-dist-ws
$ cd truffleruby-dist-ws
$ git clone https://github.com/graalvm/truffleruby.git
$ cd truffleruby
```

You should checkout a tag corresponding to a GraalVM release:
```bash
$ git checkout vm-enterprise-0.xx
```

When including Sulong, you need to specify a revision.
Find this revision from the corresponding release tag in Sulong.

```bash
export SULONG_REVISION='<FULL SULONG SHA1>'
# Or
export SULONG_REVISION='' # latest available
```

When asked which Java to use by `mx`, select the system OpenJDK.

```bash
$ ./tool/make-distribution.sh ../../truffleruby-releases minimal
$ ./tool/make-distribution.sh ../../truffleruby-releases graal
$ ./tool/make-distribution.sh ../../truffleruby-releases sulong
# full is Graal + Sulong
$ ./tool/make-distribution.sh ../../truffleruby-releases full
```

The tool relies on binary snapshots of Sulong and Truffle/Graal to be available.
If they are not, they can be re-deployed by the CI.
