# FFI

We have our own implementation of FFI, implemented entirely in Ruby + Truffle
NFI.

Using the FFI gem C extension would be suboptimal because Sulong uses Truffle
NFI to call native methods. Truffle NFI is implemented with `libffi`. The FFI C
extension uses its own `libffi` to call native functions. This means we would
use Truffle NFI's `libffi` to call the C extension `libffi` functions, adding
overhead on every call to the C extension `libffi` functions.

FFI is also one way to avoid C extensions altogether, so it makes sense to not
require C extension support for gems using FFI.

## Structure

* `lib/truffle/ffi`: Unchanged Ruby files from the gem.
* `lib/truffle/truffle/ffi_backend`: our Ruby + Truffle NFI backend for FFI,
  which replaces the C extension and has the same API.
* `lib/truffle/ffi.rb` coordinates `require`-ing the above.
* `spec/ffi`: Specs from the FFI gem.
* `src/main/ruby/truffleruby/core/truffle/ffi`: Implementation of
  `Truffle::FFI::Pointer`, which is also used in core and is aliased to
  `FFI::Pointer` once `"ffi"` is required. `Truffle::FFI::Pointer` should
  therefore have the same API as `FFI::Pointer` in the FFI gem.
* `tool/import-ffi.sh`: Imports files from a checkout of the FFI gem.

## Synchronization

I use a branch `truffleruby-specs-$FFI_RELEASE` on my fork https://github.com/eregon/ffi
which keeps our modifications on top of the FFI release tag we are based on.
`tool/import-ffi.sh` from the latest of these branches should synchronize cleanly.

In general, changes are done in TruffleRuby first, then I add them to my local `ffi` repository
in the `truffleruby-specs-$FFI_RELEASE` branch with `git cherry-pick`.
This requires `truffleruby` to be added as a remote to the `ffi` repo:
```bash
git remote add truffleruby ../truffleruby-ws/truffleruby
```

From there we should of course upstream as many changes as possible to minimize the diff.

## Running Specs in Upstream FFI Repository

```bash
chruby truffleruby
bundle install
bundle exec rspec spec/ffi
```

Note that `bundle exec rake compile` is unnecessary on TruffleRuby.

## Running FFI Tests in the TruffleRuby Repository

```bash
jt test gems ffi
```
