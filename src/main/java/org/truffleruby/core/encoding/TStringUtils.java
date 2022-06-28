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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.MutableTruffleString;
import org.jcodings.Encoding;

import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.EncodingDB;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.rope.CannotConvertBinaryRubyStringToJavaString;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.rope.TStringWithEncoding;
import org.truffleruby.core.string.StringAttributes;
import org.truffleruby.core.string.StringGuards;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;

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

    public static Rope toRope(TruffleString tstring, RubyEncoding rubyEncoding) {
        var bytes = getBytesOrCopy(tstring, rubyEncoding);
        final var rope = RopeOperations.create(bytes, rubyEncoding.jcoding, CodeRange.CR_UNKNOWN);
        assert assertEqual(rope, tstring, rubyEncoding);
        return rope;
    }

    @TruffleBoundary
    public static TruffleString fromRope(Rope rope, RubyEncoding rubyEncoding) {
        assert rope.encoding == rubyEncoding.jcoding;
        final TruffleString truffleString = fromByteArray(rope.getBytes(), rubyEncoding);
        assert assertEqual(rope, truffleString, rubyEncoding);
        return truffleString;
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

    @TruffleBoundary
    public static TruffleString fromJavaString(String javaString, RubyEncoding encoding) {
        return TruffleString.fromJavaStringUncached(javaString, encoding.tencoding);
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

    // Should be avoided as much as feasible
    public static byte[] getBytesOrCopy(AbstractTruffleString tstring, TruffleString.Encoding encoding,
            TruffleString.GetInternalByteArrayNode getInternalByteArrayNode,
            ConditionProfile noCopyProfile) {
        var bytes = tstring.getInternalByteArrayUncached(encoding);
        if (noCopyProfile.profile(tstring instanceof TruffleString && bytes.getOffset() == 0 &&
                bytes.getLength() == bytes.getArray().length)) {
            return bytes.getArray();
        } else {
            return ArrayUtils.extractRange(bytes.getArray(), bytes.getOffset(), bytes.getEnd());
        }
    }

    public static byte[] getBytesOrFail(AbstractTruffleString tstring, RubyEncoding encoding) {
        var byteArray = tstring.getInternalByteArrayUncached(encoding.tencoding);
        if (byteArray.getOffset() != 0 || byteArray.getLength() != byteArray.getArray().length) {
            throw CompilerDirectives.shouldNotReachHere();
        }
        return byteArray.getArray();
    }

    public static byte[] getBytesOrFail(AbstractTruffleString tstring, RubyEncoding encoding,
            TruffleString.GetInternalByteArrayNode byteArrayNode) {
        var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
        if (byteArray.getOffset() != 0 || byteArray.getLength() != byteArray.getArray().length) {
            throw CompilerDirectives.shouldNotReachHere();
        }
        return byteArray.getArray();
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
        final CodeRange codeRange = rope.getCodeRange();

        // Can't assert for mutable native tstrings as that would have the side effect to cache coderange & characterLength
        // without invalidating. And if we invalidate unconditionally we also have an unintended side effect which can
        // hide the lack of invalidation.
        TruffleString.CodeRange tCodeRange = truffleString.getByteCodeRangeUncached(tencoding);
        assert toCodeRange(tCodeRange) == codeRange : codeRange + " vs " + tCodeRange;

        final int characterLength = rope.characterLength();
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

    public static boolean isSingleByteOptimizable(AbstractTruffleString truffleString, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return truffleString.getByteCodeRangeUncached(encoding.tencoding) == ASCII ||
                encoding.jcoding.isSingleByte();
    }

    public static String toJavaStringOrThrow(AbstractTruffleString tstring, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        if (encoding == Encodings.BINARY && !StringGuards.is7BitUncached(tstring, encoding)) {
            int length = tstring.byteLength(encoding.tencoding);
            for (int i = 0; i < length; i++) {
                final int b = tstring.readByteUncached(i, encoding.tencoding);
                if (!Encoding.isAscii(b)) {
                    throw new CannotConvertBinaryRubyStringToJavaString(b);
                }
            }
            throw CompilerDirectives.shouldNotReachHere();
        } else {
            return tstring.toJavaStringUncached();
        }
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
