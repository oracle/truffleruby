# Testing TruffleRuby Using Docker

The `jt docker` tool can be used to generate `Dockerfiles` for testing
TruffleRuby on multiple Linux distributions in multiple configurations.

We don't use Docker in a very Docker-like way - we could be using something like
Vagrant instead.

You need to have Docker or `podman` installed.
When using `podman`, you need to set `DOCKER=podman`.

Note that running a test will fail the `build` command, so building the image
is itself a test. You can then also log into the image after it's successfully
built to use TruffleRuby.

For example, to run a full set of tests on a set of new release candidate tarballs:

```bash
jt docker test --graalvm graalvm-ce.tar.gz llvm-toolchain-installable.jar:ruby-installable-ce.jar --test release_branch
jt docker test --graalvm graalvm-ee.tar.gz llvm-toolchain-installable.jar:ruby-installable-ee.jar --test release_branch
jt docker test --graalvm graalvm-ee.tar.gz llvm-toolchain-installable.jar:ruby-installable-ee.jar --rebuild-images native-image-installable-ee.jar --test release_branch
jt docker test --standalone truffleruby-linux-amd64.tar.gz --test release_branch
```

To run tests on a specific distribution:
```bash
DOCKER=podman jt docker build --ubuntu1804 --standalone ~/Downloads/truffleruby-21.2.0-linux-amd64.tar.gz --test release/graal-vm/21.2
```

## Distributions

Pick one of the distributions in [docker-configs.yaml](../../tool/docker-configs.yaml).

## Methods of installing

Pick one of:

* From a GraalVM binary tarball and Ruby component you have locally, `--graalvm graalvm.tar.gz llvm-toolchain-installable.jar:ruby-installable.jar`
* From a TruffleRuby standalone distribution you have locally, `--standalone truffleruby-1.0.0-linux-amd64.tar.gz`

## What to do

Pick any of:

* Nothing (default)
* Run a basic test of installing and using a few gems (needs network access), `--basic-test`
* Run a full set of tests (needs network access, and a branch of the repo to test against), `--test master`

## Other options

* Print the Dockerfile rather than building it, `print` instead of `build`
* Rebuild `polyglot` and `libpolyglot` images after installing the Ruby component, `--rebuild-images`
* Run a full set of Docker tests we care about, `test` instead of `build`
* Do not rebuild `openssl`, to test error messages, `--no-rebuild-openssl`

When using `test` you need to specify the method of installing, and what to do.

## Docker cache

You may find that the Docker cache interacts badly with these Dockerfiles (such
as repository URLs being cached that become unavailable). Therefore we recommend
regularly clearing your Docker cache.

```bash
docker system prune -a -f
```

## Version incompatibilities

Some of our Docker configurations are a bit fragile, and trying to run Docker
to install old binaries, or using old versions of branches in the source
repository, may not work.
