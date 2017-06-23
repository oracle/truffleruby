# Installing `libssl`

TruffleRuby provides the `openssl` module but not the native `libssl` system library
that the module uses.

TruffleRuby requires version 1.0.1 or above (tested compatible versions include
`1.0.2e` and `1.0.2g`).

Examples of installing this on different Linux distributions:

```
yum install openssl-devel-1.0.1e                  # Oracle Linux
apt-get install -y libssl-dev=1.0.2g-1ubuntu13    # Ubuntu
```

On macOS the system version is too old. We recommend installing via
[Homebrew](https://brew.sh).

```
brew install openssl
```
