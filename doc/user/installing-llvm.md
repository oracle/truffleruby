---
layout: docs-experimental
toc_group: ruby
link_title: Installing Make and GCC
permalink: /reference-manual/ruby/InstallingLLVM/
---
# Installing Make and GCC

Since TruffleRuby 24.0.0, TruffleRuby no longer needs a LLVM toolchain and instead uses the system toolchain.
The packages below are required to build C and C++ extensions.

### Fedora-based: RHEL, Oracle Linux, etc

```bash
sudo dnf install make gcc gcc-c++
```

`gcc-c++` is only necessary for building C++ extensions.

### Debian-based: Ubuntu, etc

```bash
sudo apt-get install make gcc g++
```

`g++` is only necessary for building C++ extensions.

### macOS

On macOS, make sure you have installed the command line developer tools from Xcode:

```bash
xcode-select --install
```
