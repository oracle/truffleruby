# Installing `zlib`

TruffleRuby provides the `zlib` module but not the native `zlib` system
library that the module uses.

## Oracle Linux

```
yum install zlib-devel
```

## Ubuntu

```
apt-get install libz-dev
```

## Fedora

```
yum install zlib-devel
```

## macOS

On macOS the system version can be used.

## Remapping

If `zlib` is in a non-standard location for your system, you can use the
option:

```
-Xcexts.remap=libz.so:path/to/libz.so
```
