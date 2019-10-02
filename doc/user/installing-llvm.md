# Installing LLVM

Since TruffleRuby 19.3.0, TruffleRuby ships with its own LLVM toolchain.
Therefore, it is no longer necessary to install LLVM. If you are using an older
version, see [the documentation for that version](https://github.com/oracle/truffleruby/blob/vm-19.2.0/doc/user/installing-llvm.md).

However, for C++ extensions, it is currently still necessary to install libc++.

## Oracle Linux

For building and using C++ extensions you need to install:

```bash
$ yum-config-manager --enable ol7_developer_EPEL
$ yum install libcxx-devel
```

## Ubuntu

For building and using C++ extensions you need to install:

```bash
$ apt-get install libc++-dev libc++abi-dev    # on 18.04
$ apt-get install libc++-dev                  # on 16.04
```

Note that we install `libc++-dev` here even for just using C++ extensions, as
installing `libc++` seems to introduce some system conflicts.

## Fedora

For building and using C++ extensions you need to install:

```bash
$ sudo dnf install libcxx-devel
```

## macOS

### Development Tools and System Headers

First, make sure you have installed the command line developer tools from Xcode:

```bash
$ xcode-select --install
```

On macOS Mojave, you also have to install headers, as macOS does not provide system headers in standard locations:

```bash
$ open /Library/Developer/CommandLineTools/Packages/macOS_SDK_headers_for_macOS_10.14.pkg
```
