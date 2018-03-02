# 0.33, April 2018

New features:

* Context pre-initialization with TruffleRuby `--native`, which significantly
  improves startup time and loads the did_you_mean gem ahead of time.

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
