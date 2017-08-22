# Installing `libssl`

TruffleRuby provides the `openssl` module but not the native `libssl` system
library that the module uses.

TruffleRuby requires version 1.0.1 or above (tested compatible versions include
`1.0.2g` and `1.0.2k`, or `1.0.2l` on macOS).

## Oracle Linux

```
yum install openssl-devel-1.0.2k
```

## Ubuntu

```
apt-get install libssl-dev=1.0.2g-1ubuntu13
```

## macOS

On macOS the system version is too old. We recommend installing via
[Homebrew](https://brew.sh).

```
brew install openssl
```

## Remapping

If `libssl` is in a non-standard location for your system, you can use the
option `-Xcexts.remap=libssl.so:path/to/libssl.so`.
