# C Extensions

## Testing

Get the gem test pack.

```bash
jt gem-test-pack
```

You can then test C extension support.

```bash
jt test cexts
```

You can also runs specs:

```bash
jt test :capi
```

### OpenSSL

The `openssl` specs and tests are currently segregated and are run separately.

```bash
jt test :openssl
jt test mri --openssl
```

## Benchmarking

To run C extension benchmarks, you first need to compile them.

```bash
jt cextc bench/chunky_png/oily_png
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

### Integer and pointer data model

We currently assume an LP64 integer and pointer data model.

### Compatibility

See the user [compatibility documentation](../user/compatibility.md).
