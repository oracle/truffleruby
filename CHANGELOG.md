# 0.33, April 2018

New features:

* The Ruby version has been updated to version 2.3.6.

* Context pre-initialization with TruffleRuby `--native`, which significantly
  improves startup time and loads the `did_you_mean` gem ahead of time.
  
* The default VM is changed to SubstrateVM, where the startup is significantly 
  better. Use `--jvm` option for full JVM VM.
  
* The `REMOVABLE`, `MODIFIABLE` and `INSERTABLE` Truffle interop key info flags
  have been implemented.

* `equal?` on foreign objects will check if the underlying objects are equal
  if both are Java interop objects.

* Added a new Java-interop API similar to the one in the Nashorn JavaScript
  implementation, as also implemented by Graal.js. Includes
  `Truffle::Interop.java_type` and other methods. Needs the `--jvm` flag to be
  used.

* Supported and tested versions of LLVM for different platforms have been more
  precisely [documented](doc/user/installing-llvm.md).

Performance:

* `SecureRandom` now defers loading OpenSSL until it's needed, reducing time to
  load `SecureRandom`.

* `Array#dup` and `Array#shift` have been made constant-time operations by
  sharing the array storage and keeping a starting index.

Bug fixes:

* `Truffle::Interop.key_info` works with non-string-like names.

Internal changes:

* Changes to the lexer and translator to reduce regular expression calls.

* Some JRuby sources have been updated to 9.1.13.0.

# 0.32, March 2018

New features:

* A new embedded configuration is used when TruffleRuby is used from another
  language or application. This disables features like signals which may
  conflict with the embedding application, and threads which may conflict with
  other languages, and enables features such as the use of polyglot IO streams.

Performance:

* Conversion of ASCII-only Ruby strings to Java strings is now faster.
* Several operations on multi-byte character strings are now faster.
* Native I/O reads are ~22% faster.

Bug fixes:

* The launcher accepts `--native` and similar options in  the `TRUFFLERUBYOPT`
environment variable.

Internal changes:

* The launcher is now part of the TruffleRuby repository, rather than part of
the GraalVM repository.

* `ArrayBuilderNode` now uses `ArrayStrategies` and `ArrayMirrors` to remove
direct knowledge of array storage.

* `RStringPtr` and `RStringPtrEnd` now report as pointers for interop purposes,
fixing several issues with `char *` usage in C extensions.
