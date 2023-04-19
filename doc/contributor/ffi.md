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

`tool/import-ffi.sh` from the corresponding FFI version should synchronize cleanly.

In general, changes are done in `ffi` first, and I copy them to truffleruby via `tool/import-ffi.sh` while working on them.
We should upstream all changes we do to `ffi`.

The only diff we should have is in `src/main/ruby/truffleruby/core/truffle/ffi/pointer_extra.rb` and it should remain small.

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
