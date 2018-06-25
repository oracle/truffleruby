# Installing `libssl`

TruffleRuby provides the `openssl` module but not the native `libssl` system
library that the module uses. TruffleRuby supports both versions 1.0.2 and 1.1.0.

If you experience `openssl`-related errors, it might help to recompile the
`openssl` module by running `lib/truffle/post_install_hook.sh`.
This is done automatically by Ruby managers, and mentioned in the post-install
message when installing TruffleRuby via `gu install` in GraalVM.

## Oracle Linux

```
sudo yum install openssl-devel
```

## Ubuntu

```
apt-get install libssl-dev
```

After installing GraalVM and Ruby you will need to rebuild `openssl` for the
version in Ubuntu - run `lib/truffle/post_install_hook.sh`.

After installing GraalVM and Ruby you will need to rebuild `openssl` for the
version in Ubuntu - run `lib/truffle/post_install_hook.sh`. Make sure you run
it with TruffleRuby on your `$PATH`.

## Fedora

On Fedora 25 or more recent, the default openssl is compatible, so you only need
to install `openssl-devel`:

```
sudo dnf install openssl-devel
```

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
