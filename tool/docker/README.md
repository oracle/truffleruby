# Docker Development Images

These are Dockerfiles to build an image for developing TruffleRuby, rather than
for using it. It's useful to have a reproducible environment for build issues,
and useful for building and testing on Linux if you normally develop on macOS.

```
$ docker build -t truffleruby-dev-ubuntu .
$ docker run -it truffleruby-dev-ubuntu
```

## Oracle Linux and LLVM

Oracle Linux does not have an easy way to install the LLVM packages that we
need, so we build LLVM from source. This takes an hour or more, so we have
separated this step into a shared Dockerfile, called `oraclelinux-llvm`. You
need to build and install this image before using the Oracle Linux testing or
development Dockerfiles.

I save this image to a local file so it's not just part of my cache, since it
takes so long to build.

```
$ 
$ docker save -o oraclelinux-llvm.tar oraclelinux-llvm
$ docker load -i oraclelinux-llvm.tar
```
