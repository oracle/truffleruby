/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is modified from org.jruby.runtime.encoding.EncodingService,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.MutableTruffleString;
import org.jcodings.Encoding;

import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.EncodingDB;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.ManagedRope;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.rope.TStringWithEncoding;
import org.truffleruby.core.string.StringAttributes;

public class TStringUtils {

    @CompilationFinal(
            dimensions = 1) private static final TruffleString.Encoding[] JCODING_TO_TSTRING_ENCODINGS = createJCodingToTSEncodingMap();

    public static TruffleString.Encoding jcodingToTEncoding(Encoding jcoding) {
        return JCODING_TO_TSTRING_ENCODINGS[jcoding.getIndex()];
    }

    public static TruffleString fromByteArray(byte[] bytes, TruffleString.Encoding tencoding) {
        return TruffleString.fromByteArrayUncached(bytes, 0, bytes.length, tencoding, false);
    }

    public static TruffleString fromByteArray(byte[] bytes, RubyEncoding rubyEncoding) {
        return TruffleString.fromByteArrayUncached(bytes, 0, bytes.length, rubyEncoding.tencoding, false);
    }

    public static LeafRope toRope(AbstractTruffleString tstring, RubyEncoding rubyEncoding) {
        var bytes = getBytesOrCopy(tstring, rubyEncoding);
        final var rope = RopeOperations.create(bytes, rubyEncoding.jcoding, CodeRange.CR_UNKNOWN);
        assert assertEqual(rope, tstring, rubyEncoding);
        return rope;
    }

    @TruffleBoundary
    public static TruffleString fromRope(ManagedRope rope, RubyEncoding rubyEncoding) {
        assert rope.encoding == rubyEncoding.jcoding;
        final TruffleString truffleString = fromByteArray(rope.getBytes(), rubyEncoding);
        assert assertEqual(rope, truffleString, rubyEncoding);
        return truffleString;
    }

    public static MutableTruffleString fromRope(NativeRope rope, RubyEncoding rubyEncoding) {
        assert rope.encoding == rubyEncoding.jcoding;
        var tstring = MutableTruffleString.fromNativePointerUncached(rope.getNativePointer(), 0, rope.byteLength(),
                rubyEncoding.tencoding, false);
        assert assertEqual(rope, tstring, rubyEncoding);
        return tstring;
    }

    public static AbstractTruffleString fromRope(Rope rope, RubyEncoding rubyEncoding) {
        if (rope instanceof ManagedRope) {
            return fromRope((ManagedRope) rope, rubyEncoding);
        } else {
            return fromRope((NativeRope) rope, rubyEncoding);
        }
    }

    public static TStringWithEncoding fromRopeWithEnc(Rope rope, RubyEncoding rubyEncoding) {
        return new TStringWithEncoding(fromRope(rope, rubyEncoding), rubyEncoding);
    }

    @TruffleBoundary
    public static TruffleString utf8TString(String javaString) {
        return TruffleString.fromJavaStringUncached(javaString, TruffleString.Encoding.UTF_8);
    }

    @TruffleBoundary
    public static TruffleString usAsciiString(String javaString) {
        return TruffleString.fromJavaStringUncached(javaString, TruffleString.Encoding.US_ASCII);
    }

    @TruffleBoundary
    public static TruffleString fromJavaString(String javaString, TruffleString.Encoding encoding) {
        return TruffleString.fromJavaStringUncached(javaString, encoding);
    }

    // Should be avoided as much as feasible
    public static byte[] getBytesOrCopy(AbstractTruffleString tstring, RubyEncoding encoding) {
        var bytes = tstring.getInternalByteArrayUncached(encoding.tencoding);
        if (tstring instanceof TruffleString && bytes.getOffset() == 0 &&
                bytes.getLength() == bytes.getArray().length) {
            return bytes.getArray();
        } else {
            return ArrayUtils.extractRange(bytes.getArray(), bytes.getOffset(), bytes.getEnd());
        }
    }

    public static byte[] getBytesOrFail(AbstractTruffleString tstring, RubyEncoding encoding) {
        var bytes = tstring.getInternalByteArrayUncached(encoding.tencoding);
        if (bytes.getOffset() != 0 || bytes.getLength() != bytes.getArray().length) {
            throw CompilerDirectives.shouldNotReachHere();
        }
        return bytes.getArray();
    }

    private static boolean assertEqual(Rope rope, AbstractTruffleString truffleString, RubyEncoding rubyEncoding) {
        var tencoding = rubyEncoding.tencoding;
        assert truffleString.isCompatibleTo(tencoding);

        assert truffleString.byteLength(tencoding) == rope.byteLength();

        if (truffleString.isMutable()) {
            // make a copy for mutable strings so anything below that caches the coderange & characterLength doesn't
            // have the unintended side effect of caching them on the original mutable string,
            // which would then not be invalidated after these asserts and could report wrong
            // coderange & characterLength if mutated both before & later this method.
            // if we invalidate unconditionally we'd also have an unintended side effect which can hide the lack of invalidation.

            if (truffleString.isNative()) {
                var pointer = truffleString.getInternalNativePointerUncached(tencoding);
                truffleString = MutableTruffleString.fromNativePointerUncached(pointer, 0,
                        truffleString.byteLength(tencoding), tencoding, false);
            } else {
                var bytes = truffleString.getInternalByteArrayUncached(tencoding);
                truffleString = MutableTruffleString.fromByteArrayUncached(bytes.getArray(), bytes.getOffset(),
                        bytes.getLength(), tencoding, false);
            }
        }

        // toString() should never throw
        assert truffleString.toString() != null;

        StringAttributes stringAttributes = null;
        final CodeRange codeRange;
        if (rope instanceof NativeRope) { // ignore the cached CodeRange/characterLength which might not match
            stringAttributes = RopeOperations.calculateCodeRangeAndLength(rope.getEncoding(), rope.getBytes(), 0,
                    rope.byteLength());
            codeRange = stringAttributes.getCodeRange();
        } else {
            codeRange = rope.getCodeRange();
        }

        // Can't assert for mutable native tstrings as that would have the side effect to cache coderange & characterLength
        // without invalidating. And if we invalidate unconditionally we also have an unintended side effect which can
        // hide the lack of invalidation.
        TruffleString.CodeRange tCodeRange = truffleString.getByteCodeRangeUncached(tencoding);
        assert toCodeRange(tCodeRange) == codeRange : codeRange + " vs " + tCodeRange;

        final int characterLength = rope instanceof NativeRope
                ? stringAttributes.getCharacterLength()
                : rope.characterLength();
        assert truffleString.codePointLengthUncached(tencoding) == characterLength;
        return true;
    }

    public static CodeRange toCodeRange(TruffleString.CodeRange tCodeRange) {
        switch (tCodeRange) {
            case ASCII:
                return CodeRange.CR_7BIT;
            case VALID:
                return CodeRange.CR_VALID;
            case BROKEN:
                return CodeRange.CR_BROKEN;
            default:
                throw CompilerDirectives.shouldNotReachHere(tCodeRange.name());
        }
    }

    @TruffleBoundary
    public static boolean isSingleByteOptimizable(AbstractTruffleString truffleString, RubyEncoding encoding) {
        return truffleString.getByteCodeRangeUncached(encoding.tencoding) == TruffleString.CodeRange.ASCII ||
                encoding.jcoding.isSingleByte();
    }

    private static TruffleString.Encoding[] createJCodingToTSEncodingMap() {
        var map = new TruffleString.Encoding[EncodingDB.getEncodings().size()];
        for (var entry : EncodingDB.getEncodings()) {
            var jcoding = entry.getEncoding();
            var jcodingName = jcoding.toString();
            final TruffleString.Encoding tsEncoding;
            if (jcodingName.equals("UTF-16")) {
                tsEncoding = TruffleString.Encoding.UTF_16; // is it OK? jcoding one is dummy
            } else if (jcodingName.equals("UTF-32")) {
                tsEncoding = TruffleString.Encoding.UTF_32; // is it OK? jcoding one is dummy
            } else {
                tsEncoding = TruffleString.Encoding.fromJCodingName(jcodingName);
            }
            map[jcoding.getIndex()] = tsEncoding;
        }
        return map;
    }
}
