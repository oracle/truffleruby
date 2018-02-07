# Installing `zlib`

TruffleRuby provides the `zlib` module but not the native `zlib` system
library that the module uses.

## Linux

On all Linux distributions we have tested, `zlib` is always going to be
installed by other packages that you need to install, so we have never needed to
install it manually.

## macOS

On macOS the system version can be used.

## Remapping

If `zlib` is in a non-standard location for your system, you can use the
option:

```
-Xcexts.remap=libz.so:path/to/libz.so
```
