# TruffleRuby, Sulong and C extensions

TruffleRuby runs C extension using Sulong. You should build Sulong from source.
Set `SULONG_VERSION=3.8` when building. Clone from 
https://github.com/graalvm/sulong

You need LLVM installed. Version 3.8 seems to work best. We need the `opt`
command, so you can't just use what is installed by Xcode if you are on macOS,
and you'll need to install LLVM via something like Homebrew. E.g. use 
`brew install llvm38`.

You can now build the C extension support. Building the OpenSSL C extension is
incomplete, so most people probably want to disable that with `--no-openssl`.
The 3 environment variables in the following command are needed, `JT_CLANG` and
`JT_OPT` point the the LLVM binaries installed earlier. 

```bash
SULONG_HOME=/absolute/path/to/sulong JT_OPT=opt-3.8 JT_CLANG=clang-3.8 \
    jt build cexts --no-openssl
```

Get the `jruby-truffle-gem-test-pack` repository at 
https://github.com/jruby/jruby-truffle-gem-test-pack

You can then test C extension support.

```bash
SULONG_HOME=/absolute/path/to/sulong JT_OPT=opt-3.8 JT_CLANG=clang-3.8 GEM_HOME=../jruby-truffle-gem-test-pack/gems \
    jt test cexts --no-libxml --no-openssl
```

If you want to test `libxml`, remove that flag and set either `LIBXML_HOME` or
`LIBXML_INCLUDE` and `LIBXML_LIB`. Try the same with `OPENSSL_` if you are
adventurous.

We further assume that following was exported: 
`SULONG_HOME=/absolute/path/to/sulong`, `JT_OPT=opt-3.8`, `JT_CLANG=clang-3.8`. 

You can also runs specs:

```bash
jt test --sulong :capi
```

To run C extension benchmarks, you first need to compile them.

```bash
jt cextc .../all-ruby-benchmarks/chunky_png/oily_png/
```

Then follow the instructions for benchmarking above, and then try:

```bash
USE_CEXTS=true TRUFFLERUBYOPT=-Xcexts.log.load=true jt benchmark .../all-ruby-benchmarks/chunky_png/chunky-color-r.rb --simple
```

These benchmarks have Ruby fallbacks, so we should carefully check that the
C extension is actually being used by looking for these log lines.

```
[ruby] INFO loading cext module ...
```
