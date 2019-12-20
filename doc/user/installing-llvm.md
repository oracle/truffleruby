# Installing LLVM

Since TruffleRuby 19.3.0, TruffleRuby ships with its own LLVM toolchain.
Therefore, it is no longer necessary to install LLVM. If you are using an older
version, see [the documentation for that version](https://github.com/oracle/truffleruby/blob/vm-19.2.0/doc/user/installing-llvm.md).

The `make` utility as well as the dependencies of the toolchain still need to be available to build C and C++ extensions.

## RedHat-based: Fedora, Oracle Linux, etc

```bash
$ sudo dnf install make gcc
```

## Debian-based: Ubuntu, etc

```bash
$ sudo apt-get install make gcc
```

## macOS

On macOS, make sure you have installed the command line developer tools from Xcode:

```bash
$ xcode-select --install
```
