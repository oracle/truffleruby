# Docker Development Images

These are Dockerfiles to build an image for developing TruffleRuby, rather than
for using it. It's useful to have a reproducible environment for build issues,
and useful for building and testing on Linux if you normally develop on macOS.

It also serves as a nice executable documentation of how to build TruffleRuby
from scratch, on different distributions. We have images for OracleLinux, which
should translate to other distributions based on the RedHat architecture, and
Ubuntu, which should translate to other distributions based on the Debian
architecture.

We've tried to document versions of everything in here to keep it really stable,
but our source repositories aren't versioned. If you are trying to make a
reproducible build you should note down the versions of these. We've also tried
to list the minimal set of packages needed.

```
$ docker build -t truffleruby-dev-oraclelinux .
$ docker run -it truffleruby-dev-oraclelinux
```
