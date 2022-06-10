---
layout: docs-experimental
toc_group: ruby
link_title: Installing `libssl`
permalink: /reference-manual/ruby/Installinglibssl/
---
# Installing `libssl`

TruffleRuby provides the `openssl` gem but not the native `libssl` system library that the gem uses.
TruffleRuby supports libssl versions 1.0.2, 1.1.0 and 3.0.0.

If you experience `openssl`-related errors, it might help to recompile the `openssl` gem by running `lib/truffle/post_install_hook.sh`.
This is done automatically by Ruby managers, and mentioned in the post-install message when installing TruffleRuby via `gu install` in GraalVM.

To compile TruffleRuby against a non-system `libssl`, set `OPENSSL_PREFIX` while installing TruffleRuby:
```bash
export OPENSSL_PREFIX=/path/to/my/openssl-1.1.0
```

### RedHat-based: Fedora, Oracle Linux, etc

```bash
sudo dnf install openssl-devel
```

### Debian-based: Ubuntu, etc

```bash
sudo apt-get install libssl-dev
```

### macOS

On macOS the system version is too old.

#### Homebrew

We recommend installing libssl via [Homebrew](https://brew.sh).

```bash
brew install openssl
```

#### MacPorts

MacPorts should also work but is not actively tested.

```bash
sudo port install openssl
```
