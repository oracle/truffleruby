# TruffleRuby, Sulong and C extensions

## Setup

TruffleRuby runs C extension using Sulong. You should build Sulong from source.
Clone from https://github.com/graalvm/sulong

You need LLVM installed. Version 3.8 seems to work best. We need the `opt`
command, so you can't just use what is installed by Xcode if you are on macOS,
and you'll need to install LLVM via something like Homebrew. E.g. use
`brew install llvm38`.

You can now build the C extension support. Building the OpenSSL C extension is
incomplete, so most people probably want to disable that with `--no-openssl`.
The 3 environment variables in the following command are needed, `JT_CLANG` and
`JT_OPT` point the the LLVM binaries installed earlier.
We further assume these variables are exported.

```bash
export SULONG_HOME=/absolute/path/to/sulong
export JT_OPT=opt-3.8
export JT_CLANG=clang-3.8
jt build cexts --no-openssl
```

## Testing

Get the `truffleruby-gem-test-pack-n.tar.gz` file and extract into the root
of the TruffleRuby repository.

You can then test C extension support.

```bash
GEM_HOME=truffleruby-gem-test-pack/gems jt test cexts --no-libxml --no-openssl
```

If you want to test `libxml`, remove that flag and set either `LIBXML_HOME` or
`LIBXML_INCLUDE` and `LIBXML_LIB`. Try the same with `OPENSSL_` if you are
adventurous.

You can also runs specs:

```bash
jt test --sulong :capi
```

## Benchmarking

To run C extension benchmarks, you first need to compile them.

```bash
jt cextc bench/chunky_png/oily_png/
```

Then follow the instructions for benchmarking above, and then try:

```bash
USE_CEXTS=true TRUFFLERUBYOPT=-Xcexts.log.load=true jt benchmark bench/chunky_png/chunky-color-r.rb --simple
```

These benchmarks have Ruby fallbacks, so we should carefully check that the
C extension is actually being used by looking for these log lines.

```
[ruby] INFO loading cext module ...
```

## OpenSSL

To build the `openssl` gem you need to install the OpenSSL system library and configure the
location to the OpenSSL header and library files. On macOS we use Homebrew and 1.0.2g.
On Linux, it's probably safe to use your package manager's version of the OpenSSL dev package
(just note that we currently test with 1.0.2g).
For Homebrew you can simply set the `OPENSSL_HOME` variable to the Cellar location.
For Linux users installing system packages, you may need
to set one or more of `OPENSSL_LIB_HOME`, `OPENSSL_INCLUDE`, or `OPENSSL_LIB`.
On Ubuntu, setting `OPENSSL_LIB_HOME` is sufficient.
Once your environment variables are set, you can build the C extensions, including support for `openssl`.
Note that if you do change any of the aforementioned environment variables you will need to recompile the extension.

```bash
# macOS
OPENSSL_HOME=/usr/local/Cellar/openssl/1.0.2g jt build cexts

# Linux, Ubuntu
OPENSSL_LIB_HOME=/usr/lib/x86_64-linux-gnu jt build cexts
# Linux, Fedora
OPENSSL_LIB_HOME=/usr/lib64 jt build cexts
```

The `openssl` specs and tests are currently segregated and are run separately.
We have patches to workaround `openssl` so you need to disable these to
actually test it, with `-T-Xpatching=false`.

```bash
jt test -T-Xpatching=false --sulong :openssl
jt test mri --openssl --sulong
```

## Tools

The `tool/cext-compile-explore.rb` script can be used to show the output of
stages of the C preprocessor and LLVM IR.

## Implementation

We run Ruby C extensions using Sulong, running any dynamically linked libraries
natively.

### Compilation

We compile C extensions using the standard `mkmf` tool, and `clang` compilers
which have flags set to generate bitcode instead of machine code. We separately
run the LLVM `opt` tool to specifically apply some optimizations that we need
such as `mem2reg`. We link the bitcode files into a `.su` file (just a JAR of
the bitcode files), using the `su-link` tool in Sulong.

We pipe C source code through a pre-processor `lib/cext/preprocess.rb` before it
goes to `clang` to workaround some limitations.

### API functions

Most API functions are defined in the C header file, the C implementation file,
and then either implemented as a call to a method on one of the arguments using
`truffle_invoke` to do a foreign call from C into Ruby, or we implement the
function in Ruby in the `Truffle::CExt` module and use `truffle_invoke` to call
that.

Some functions are re-implemented as macros, and some macros are re-implemented
as functions, where this makes sense.

### Array and string pointers

When a pointer to the storage for an array of a string is taken using
`RARRAY_PTR` or `RSTRING_PTR` a proxy object is created that pretends to be a C
pointer but really redirects interop reads and writes from C back to the
original object. The array pointer cannot be passed to native, but the string
pointer can be, by permanently converting the rope implementing the string to
native memory and returning the pointer to this memory.

### Data and typed data

The user data pointer for data and typed data objects needs to be available as a
right-hand-side value which can be assigned to, so we cannot implement this as a
function call. Instead, when a data or typed data object is cast to pointers to
the `RData` or `RTypedData` structures we create a proxy object that pretends to
be a C pointer but really redirects interop reads and writes from C to read and
write the data field in the object.

### `rb_scan_args`

`rb_scan_args` is problematic because it passes pointers to local variables in
variadic arguments. Taking the address of a local variable that holds a Ruby
object and passing that to a function which is not inlined is not supported, and
`clang` will never inline a function with variadic arguments.

We work around this by defining `rb_scan_args` as a macro that redirects to a
function that is not variadic and handles up to ten pointers. This function is
then inlined.

### Compatibility

See the user [compatibility documentation](../user/compatibility.md).
