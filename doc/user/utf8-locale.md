---
layout: docs-experimental
toc_group: ruby
link_title: Setting up a UTF-8 Locale
permalink: /reference-manual/ruby/UTF8Locale/
---
# Setting Up a UTF-8 Locale

You need a UTF-8 locale to run some Ruby applications.
For example, we have found that RubyGems and ruby/spec need such a locale.

This is not needed if the `$LANG` environment variable is already set and:

```bash
locale
```

shows no `="C"` and no warning.
Instead, all values should be `"en_US.UTF-8"` or other regions but still `.UTF-8`.

### RedHat-based: Fedora, Oracle Linux, etc

```bash
export LANG=en_US.UTF-8
```

### Debian-based: Ubuntu, etc

#### Ubuntu

The Ubuntu version of `locale-gen` supports arguments, so it is easy:
```bash
sudo apt-get install -y locales
sudo locale-gen en_US.UTF-8
export LANG=en_US.UTF-8
```

#### Non-Ubuntu

Debian and other non-Ubuntu do not support `locale-gen` arguments.
Instead you need to modify `/etc/locale.gen`:
```bash
# Uncomment the en_US.UTF-8 line in /etc/locale.gen
sudo sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen

# locale-gen generates locales for all uncommented locales in /etc/locale.gen
sudo locale-gen
export LANG=en_US.UTF-8
```

For Dockerfile's:

```dockerfile
# Uncomment the en_US.UTF-8 line in /etc/locale.gen
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen
# locale-gen generates locales for all uncommented locales in /etc/locale.gen
RUN locale-gen
ENV LANG=en_US.UTF-8
```
