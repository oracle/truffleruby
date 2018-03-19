# Installing LLVM

TruffleRuby needs LLVM to run and build C extensions. The versions that are
tested for each platform and how to install them are documented below. You may
have success with later versions, but we don't actively test these.

## Oracle Linux

The tested version of LLVM for Oracle Linux is 4.0.1.

Oracle Linux does not include recent-enough LLVM packages, so you will have to
[build LLVM from scratch](https://llvm.org/docs/CMake.html). You'll need to
include at least the `libcxx` and `libcxxabi` packages for running, and `clang`
for building. One way to build it is documented in the
`tool/docker/oraclelinux-llvm` Dockerfile in the TruffleRuby source repository.

## Ubuntu

The tested version of LLVM for Ubuntu is 3.8.

For using C extensions:

```
apt-get install libc++-dev libc++abi1
```

Note that we install `libc++-dev` here, as installing `libc++` seems to
introduce some system conflicts.

Additionally, for building C extensions:

```
apt-get install clang llvm libc++abi-dev
```

## Fedora

The tested version of LLVM for Fedora is 4.0.1.

For using C extensions:

```
sudo dnf install libcxx libcxxabi
```

Additionally, for building C extensions:

```
sudo dnf install clang llvm libcxx-devel
```

## macOS

The tested version of LLVM for macOS is 4.0.1.

We need the `opt` command, so you can't just use what is installed by Xcode if
you are on macOS. We would recommend that you install LLVM 4 via
[Homebrew](https://brew.sh) and then manually set your path.

```bash
brew install llvm@4
export PATH="/usr/local/opt/llvm@4/bin:$PATH"
```
