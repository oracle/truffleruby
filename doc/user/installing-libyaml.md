---
layout: docs-experimental
toc_group: ruby
link_title: Installing LibYAML
permalink: /reference-manual/ruby/InstallingLibYAML/
---
# Installing LibYAML

TruffleRuby requires to have `libyaml` installed, much like CRuby 3.2+ and Psych 5+.

If you experience `psych`-related errors saying it cannot find `libyaml`, it might help to recompile the `psych` gem by running `lib/truffle/post_install_hook.sh`.
This is done automatically by Ruby managers, and mentioned in the post-install message when installing TruffleRuby via `gu install` in GraalVM.

### Fedora-based: RHEL, Oracle Linux, etc

#### Fedora

```bash
sudo dnf install libyaml-devel
```

#### Oracle Linux 7

```bash
sudo yum install --enablerepo=ol7_optional_latest libyaml-devel
```

#### Oracle Linux 8

```bash
sudo dnf install --enablerepo=ol8_codeready_builder libyaml-devel
```

### Debian-based: Ubuntu, etc

```bash
sudo apt-get install libyaml-dev
```

### macOS

#### Homebrew

We recommend installing libssl via [Homebrew](https://brew.sh).

```bash
brew install libyaml
```

#### MacPorts

MacPorts should also work but is not actively tested.

```bash
sudo port install libyaml
```
