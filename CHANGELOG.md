# 0.32 (in development)

New features:

* ...

Bug fixes:

* ...

Performance:

* Conversion of ASCII-only Ruby strings to Java strings is now faster.

Internal changes:

* The launcher is now part of the TruffleRuby repository, rather than part of
the GraalVM repository.

* ArrayBuilderNode now uses ArrayStrategies and ArrayMirrors to remove
direct knowledge of array storage.
