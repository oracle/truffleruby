# Installing `libssl`

TruffleRuby provides the `openssl` module but not the native `libssl` system
library that the module uses. TruffleRuby requires version 1.0.2.

## Ubuntu

```
apt-get install libssl-dev
```

We recommend `libssl-dev` instead of `libssl1.0.0`, even though we aren't using
it for development because the latter doesn't seem to put the shared library in
a place where we can find it.

## macOS

On macOS the system version is too old. We recommend installing via
[Homebrew](https://brew.sh).

```
brew install openssl
```

## Remapping

If `libssl` is in a non-standard location for your system, you can use the
option
```
-Xcexts.remap=libssl.so:path/to/libssl.so
```
