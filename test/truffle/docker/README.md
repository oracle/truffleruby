# Docker Test Images

These are Dockerfiles to test TruffleRuby builds on different Linux
distributions. They could also serve as sources on which to base a deployment
image, but we'd recommend the official GraalVM Dockerfile for that.

https://github.com/oracle/docker-images/tree/master/GraalVM

We have images for OracleLinux, which should translate to other distributions
based on the RedHat architecture, and Ubuntu, which should translate to other
distributions based on the Debian architecture.

These Dockerfiles run the tests as part of the build, so if the image
successfully builds then the tests have all passed.

You need to put the built GraalVM binary tarball into the same directory as the
Dockerfile, and update the Dockerfile for the version.

```
$ docker build -t truffleruby-test-oraclelinux .
```
