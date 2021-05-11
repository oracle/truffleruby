---
layout: docs-experimental
toc_group: ruby
title: Installing `zlib`
link_title: Installing `zlib`
permalink: /reference-manual/ruby/Installingzlib/
redirect_from: /docs/reference-manual/ruby/Installingzlib/
next: /en/graalvm/enterprise/{{ site.version }}/docs/reference-manual/ruby/JRubyMigration/
previous: /en/graalvm/enterprise/{{ site.version }}/docs/reference-manual/ruby/InstallingLLVM/
---
# Installing `zlib`

TruffleRuby provides the `zlib` module but not the native `zlib` system library that the module uses.

### RedHat-based: Fedora, Oracle Linux, etc

```bash
sudo dnf install zlib-devel
```

### Debian-based: Ubuntu, etc

```bash
sudo apt-get install libz-dev
```

### macOS

On macOS the system version can be used.
