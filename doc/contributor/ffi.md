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
