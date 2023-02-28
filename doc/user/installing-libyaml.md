---
layout: docs-experimental
toc_group: ruby
link_title: Installing LibYAML
permalink: /reference-manual/ruby/InstallingLibYAML/
---
# Installing LibYAML

TruffleRuby requires to have `libyaml` installed, much like CRuby 3.2+ and Psych 5+.

### Fedora-based: RHEL, Oracle Linux, etc

```bash
sudo dnf install libyaml-devel
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
