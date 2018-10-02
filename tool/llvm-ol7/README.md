This is a Dockerfile to build a binary tarball of LLVM for Oracle Linux 7.

```
$ docker build -t llvm-ol7 .
$ docker create llvm-ol7
$ docker cp [hash]:/build/llvm-3.8.0-ol7.tar.gz llvm-3.8.0-ol7.tar.gz
```
