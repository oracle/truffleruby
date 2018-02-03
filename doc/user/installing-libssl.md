# Installing `libssl`

TruffleRuby provides the `openssl` module but not the native `libssl` system
library that the module uses. TruffleRuby requires version 1.0.2.

Version 1.1.0 or more recent is incompatible, so you might need to install a
compatibility version of `libssl`.

## Oracle Linux

```
sudo yum install openssl-devel
```

## Ubuntu

```
apt-get install libssl-dev
```

We recommend `libssl-dev` instead of `libssl1.0.0`, even though we aren't using
it for development because the latter doesn't seem to put the shared library in
a place where we can find it.

## Fedora

On Fedora 25 or older, the default openssl is correct, so you only need to
install `openssl-devel`:

```
sudo dnf install openssl-devel
```

However, on Fedora 26 or more recent, the default openssl is 1.1.0 which is too
recent. A workaround is to install the compatibility openssl 1.0.

```
# Make sure the 1.1.0 headers are not installed as they would conflict
sudo dnf remove openssl-devel
# Install openssl 1.0
sudo dnf install compat-openssl10-devel
```

We recommend `compat-openssl10-devel` instead of `compat-openssl10`, because the
latter doesn't seem to put the shared library in a place where we can find it.

## macOS

On macOS the system version is too old. We recommend installing via
[Homebrew](https://brew.sh).

```
brew install openssl
```

## Remapping

If `libssl` is in a non-standard location for your system, you can use the
option:

```
-Xcexts.remap=libssl.so:path/to/libssl.so
```
