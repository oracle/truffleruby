# Installing LLVM

TruffleRuby needs LLVM to run and build C extensions - version 3.8 or 4.0.

## Ubuntu

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

## macOS

We need the `opt` command, so you can't just use what is installed by Xcode if
you are on macOS. We would recommend that you install LLVM via
[Homebrew](https://brew.sh) and then manually set your path.

```bash
brew install llvm
export PATH="/usr/local/opt/llvm/bin:$PATH"
```
