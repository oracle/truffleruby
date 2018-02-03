# Setting up a UTF-8 locale

You may need a UTF-8 locale to run some Ruby applications. For example we've
found that Ruby Spec needs such a locale.

## Oracle Linux

```
export LANG=en_US.UTF-8
```

## Ubuntu

```
apt-get install -y locales
locale-gen en_US.UTF-8
export LANG=en_US.UTF-8
```

## Fedora

```
export LANG=en_US.UTF-8
```
