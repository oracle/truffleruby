# TruffleString in TruffleRuby

TruffleRuby uses `TruffleString` to represent Ruby Strings, but wraps them in either a RubyString or a ImmutableRubyString object.

## Encodings Compatibility

The notion of encodings compatibility is mostly the same between Ruby and TruffleString but differs in one point:
* An empty Ruby String is always considered compatible with any other Ruby String of any encoding.
* TruffleString does not consider whether a string is empty or not, and only look at their encodings and code range.

As a result, to use TruffleString equality nodes, one needs to:
1. Compute the compatible encoding with `NegotiateCompatibleStringEncodingNode` or `Primitive.encoding_ensure_compatible_str`.
2. Check if both sides are empty, and if so return true before using TruffleString equality nodes.

`StringHelperNodes.StringEqualInternalNode` is a good example showing what is needed.

An example which would throw without empty checks is comparing an empty ISO-2022-JP (a dummy, non-ascii-compatible, fixed-width encoding) string with an empty US-ASCII string:

```bash
$ jt ruby -e '"".force_encoding("ISO-2022-JP") == ""'
the given string is not compatible to the expected encoding "ISO_2022_JP", did you forget to convert it? (java.lang.IllegalArgumentException)
```

## Logical vs Physical Byte Offsets

We categorize a byte offset into a `TruffleString` as either *logical* or *physical*.
A physical byte offset includes the offset from the `InternalByteArray` (`InternalByteArray#getOffset()`).
A logical byte offset does not include that and is the semantic byte offset from the start of the string.
Physical offsets are quite difficult to use and they are error-prone as they can be passed by mistake to a method taking a logical offset.
So avoid physical offsets as much as possible, and therefore avoid `InternalByteArray#getArray()`.

## Tests

This is a good set of tests to run when touching String code:
```
jt test integration strict-encoding-checks
```
