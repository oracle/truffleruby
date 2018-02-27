# 0.32, March 2018

New features:

* A new embedded configuration is used when TruffleRuby is used from another
  language or application. This disables features like signals which may
  conflict with the embedding application, and threads which may conflict with
  other languages, and enables features such as the use of polyglot IO streams.

Performance:

* Conversion of ASCII-only Ruby strings to Java strings is now faster.

Bug fixes:

* The launcher accepts `--native` and similar options in  the `TRUFFLERUBYOPT` 
environment variable.

Internal changes:

* The launcher is now part of the TruffleRuby repository, rather than part of
the GraalVM repository.

* `ArrayBuilderNode` now uses `ArrayStrategies` and `ArrayMirrors` to remove
direct knowledge of array storage.
