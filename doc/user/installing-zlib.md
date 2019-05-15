# Installing `zlib`

TruffleRuby provides the `zlib` module but not the native `zlib` system
library that the module uses.

## Oracle Linux

```bash
$ yum install zlib-devel
```

## Ubuntu

```bash
$ apt-get install libz-dev
```

## Fedora

```bash
$ yum install zlib-devel
```

## macOS

On macOS the system version can be used.
