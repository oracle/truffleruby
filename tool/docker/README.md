# Docker Development Images

These are Dockerfiles to build an image for developing TruffleRuby, rather than
for using it. It's useful to have a reproducible environment for build issues,
and useful for building and testing on Linux if you normally develop on macOS.

Note that the Oracle Linux Dockerfile takes much longer to run, as it builds
LLVM from source.

```
$ docker build -t truffleruby-dev-ubuntu .
$ docker run -it truffleruby-dev-ubuntu
```
