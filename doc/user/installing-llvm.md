# Installing LLVM

TruffleRuby needs LLVM to build C extensions - version 3.8 or above we think but
we haven't tested many versions.

## Oracle Linux

```bash
yum install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
yum install clang-3.4.2
```

## Ubuntu

```bash
apt-get install clang=1:3.8-33ubuntu3.1 llvm-dev=1:3.8-33ubuntu3.1 libc++-dev=3.7.0-1 libc++abi-dev=3.7.0-1
```

## macOS

We need the `opt` command, so you can't just use what is installed by Xcode if
you are on macOS. We would recommend that you install LLVM via
[Homebrew](https://brew.sh) and then manually set your path.

```bash
brew install llvm
export PATH="/usr/local/opt/llvm/bin:$PATH"
```
