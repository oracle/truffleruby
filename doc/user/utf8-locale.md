# Setting up a UTF-8 Locale

You may need a UTF-8 locale to run some Ruby applications. For example we've
found that Ruby Spec needs such a locale.

### RedHat-based: Fedora, Oracle Linux, etc

```bash
export LANG=en_US.UTF-8
```

### Debian-based: Ubuntu, etc

```bash
sudo apt-get install -y locales
sudo locale-gen en_US.UTF-8
export LANG=en_US.UTF-8
```
