---
layout: docs-experimental
toc_group: ruby
link_title: Installing Make and GCC
permalink: /reference-manual/ruby/InstallingLLVM/
redirect_from: /docs/reference-manual/ruby/InstallingLLVM/
---
# Installing Make and GCC

Since TruffleRuby 19.3.0, TruffleRuby ships with its own LLVM toolchain.
Therefore, it is no longer necessary to install LLVM.
If you are using an older version, see [the documentation for that version](https://github.com/oracle/truffleruby/blob/vm-19.2.0/doc/user/installing-llvm.md).

The `make` utility as well as the dependencies of the toolchain still need to be available to build C and C++ extensions.

## RedHat-based: Fedora, Oracle Linux, etc.

```bash
sudo dnf install make gcc
```

## Debian-based: Ubuntu, etc.

```bash
sudo apt-get install make gcc
```

## Mandriva-based and other Linux distributions

Note: Such distributions are not tested and not [supported](../../README.md#system-compatibility).

First, install the `make` and `gcc` dependencies.

Mandriva uses a not-yet-upstreamed patch to let `clang` find the GCC installation (see [this comment](https://github.com/oracle/truffleruby/issues/2009#issuecomment-630019082)).
Therefore the internal LLVM toolchain cannot find the necessary `libgcc_s` by default.
The proper fix is for those distributions to upstream their changes to LLVM.

A workaround is to create a symlink explicitly so that the LLVM toolchain can find `libgcc_s`:
```bash
cd /usr/lib/gcc
sudo ln -s x86_64-mandriva-linux-gnu x86_64-linux-gnu
```

## macOS

On macOS, make sure you have installed the command line developer tools from Xcode:

```bash
xcode-select --install
```

You might need to add `export SDKROOT=$(xcrun --show-sdk-path)` in your shell profile.
