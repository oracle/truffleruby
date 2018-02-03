# Creating a TruffleRuby distribution

See the [Distributions page](../user/distribution.md).

The tool to build these distributions is `tool/make-distribution.sh`.

This script should be run in a fresh checkout of truffleruby in a new workspace.
This is necessary as otherwise extra gems and build artifacts might be included.

```bash
$ mkdir truffleruby-dist-ws
$ cd truffleruby-dist-ws
$ git clone https://github.com/oracle/truffleruby.git
$ cd truffleruby
```

You should checkout a tag corresponding to a GraalVM release:
```bash
$ git checkout vm-enterprise-0.xx
```

Set `JVMCI_VERSION` to the JVMCI version used in the GraalVM release:

```bash
export JVMCI_VERSION=jvmci-0.38
```

Then you should update the Truffle revision in `mx.truffleruby/suite.py` to
match the revision of `truffle` in the GraalVM `release` file.

When including Sulong, you need to specify a revision.
Find this revision in the GraalVM `release` file.

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
