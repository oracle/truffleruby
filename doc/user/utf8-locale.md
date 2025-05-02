---
layout: docs-experimental
toc_group: ruby
link_title: Setting up a UTF-8 Locale
permalink: /reference-manual/ruby/UTF8Locale/
---
# Setting Up a UTF-8 Locale

Since TruffleRuby 25.0, TruffleRuby supports the `POSIX` locale, the default locale in Docker images.
**So there is no need to set up a locale anymore.**

Some Ruby applications however require setting up a proper locale (same on CRuby).
The instructions below explain how to do that.

You can check the current locale using:

```bash
locale
```

If that shows warnings, it probably means `LANG` is set to a locale which is not installed.

These docs explain how to setup the `en_US.UTF-8` locale.

As a note, the `C.UTF-8` locale also exists on Linux (but not on macOS) and might be more convenient as it does not require installing extra packages.

### Fedora-based: RHEL, Oracle Linux, etc

```bash
sudo dnf install glibc-langpack-en
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
