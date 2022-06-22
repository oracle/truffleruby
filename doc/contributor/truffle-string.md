# TruffleString in TruffleRuby

TruffleRuby uses `TruffleString` to represent Ruby Strings, but wraps them in either a RubyString or a ImmutableRubyString object.

## Encodings Compatibility

The notion of encodings compatibility is mostly the same between Ruby and TruffleString but differs in one point:
* An empty Ruby String is always considered compatible with any other Ruby String of any encoding.
* TruffleString does not consider whether a string is empty or not, and only look at their encodings and code range.

As a result, to use TruffleString equality nodes, one needs to:
1. Compute the compatible encoding with `NegotiateCompatibleStringEncodingNode` or `Primitive.encoding_ensure_compatible_str`.
2. Check if both sides are empty, and if so return true before using TruffleString equality nodes.

`StringNodes.StringEqualNode` is a good example showing what is needed.
