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
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import org.jcodings.Encoding;

import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.EncodingDB;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.string.CannotConvertBinaryRubyStringToJavaString;
import org.truffleruby.core.string.StringGuards;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;

public class TStringUtils {

    @CompilationFinal(
            dimensions = 1) private static final TruffleString.Encoding[] JCODING_TO_TSTRING_ENCODINGS = createJCodingToTSEncodingTable();

    public static TruffleString.Encoding jcodingToTEncoding(Encoding jcoding) {
        return JCODING_TO_TSTRING_ENCODINGS[jcoding.getIndex()];
    }

    public static TruffleString fromByteArray(byte[] bytes, TruffleString.Encoding tencoding) {
        CompilerAsserts.neverPartOfCompilation(
                "Use createString(TruffleString.FromByteArrayNode, byte[], RubyEncoding) instead");
        return TruffleString.fromByteArrayUncached(bytes, 0, bytes.length, tencoding, false);
    }

    public static TruffleString fromByteArray(byte[] bytes, RubyEncoding rubyEncoding) {
        return fromByteArray(bytes, rubyEncoding.tencoding);
    }

    public static TruffleString utf8TString(String javaString) {
        return fromJavaString(javaString, TruffleString.Encoding.UTF_8);
    }

    public static TruffleString usAsciiString(String javaString) {
        return fromJavaString(javaString, TruffleString.Encoding.US_ASCII);
    }

    public static TruffleString fromJavaString(String javaString, TruffleString.Encoding encoding) {
        CompilerAsserts.neverPartOfCompilation(
                "Use createString(TruffleString.FromJavaStringNode, String, RubyEncoding) instead");
        return TruffleString.fromJavaStringUncached(javaString, encoding);
    }

    public static TruffleString fromJavaString(String javaString, RubyEncoding encoding) {
        return fromJavaString(javaString, encoding.tencoding);
    }

    // Should be avoided as much as feasible
    public static byte[] getBytesOrCopy(AbstractTruffleString tstring, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("uncached");
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
        var bytes = getInternalByteArrayNode.execute(tstring, encoding);
        if (noCopyProfile.profile(tstring instanceof TruffleString && bytes.getOffset() == 0 &&
                bytes.getLength() == bytes.getArray().length)) {
            return bytes.getArray();
        } else {
            return ArrayUtils.extractRange(bytes.getArray(), bytes.getOffset(), bytes.getEnd());
        }
    }

    private static final boolean DEBUG_NON_ZERO_OFFSET = Boolean
            .getBoolean("truffle.strings.debug-non-zero-offset-arrays");

    public static byte[] getBytesOrFail(AbstractTruffleString tstring, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("uncached");
        if (DEBUG_NON_ZERO_OFFSET) {
            return getBytesOrCopy(tstring, encoding);
        } else {
            var byteArray = tstring.getInternalByteArrayUncached(encoding.tencoding);
            if (byteArray.getOffset() != 0 || byteArray.getLength() != byteArray.getArray().length) {
                throw CompilerDirectives.shouldNotReachHere();
            }
            return byteArray.getArray();
        }
    }

    public static byte[] getBytesOrFail(AbstractTruffleString tstring, RubyEncoding encoding,
            TruffleString.GetInternalByteArrayNode byteArrayNode) {
        if (DEBUG_NON_ZERO_OFFSET) {
            return getBytesOrCopy(tstring, encoding);
        } else {
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            if (byteArray.getOffset() != 0 || byteArray.getLength() != byteArray.getArray().length) {
                throw CompilerDirectives.shouldNotReachHere();
            }
            return byteArray.getArray();
        }
    }

    public static boolean isSingleByteOptimizable(AbstractTruffleString truffleString, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("Use SingleByteOptimizableNode instead");
        return truffleString.getByteCodeRangeUncached(encoding.tencoding) == ASCII ||
                encoding.jcoding.isSingleByte();
    }

    public static String toJavaStringOrThrow(AbstractTruffleString tstring, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("uncached");
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

    private static TruffleString.Encoding[] createJCodingToTSEncodingTable() {
        var map = new TruffleString.Encoding[EncodingDB.getEncodings().size()];
        for (var entry : EncodingDB.getEncodings()) {
            var jcoding = entry.getEncoding();
            var jcodingName = jcoding.toString();
            final TruffleString.Encoding tsEncoding;
            if (jcodingName.equals("UTF-16")) {
                // We use UTF_16BE because JCodings resolves UTF-16 to UTF16BEEncoding(dummy=true)
                // See org.jcodings.EncodingDB.dummy_unicode
                tsEncoding = TruffleString.Encoding.UTF_16BE;
            } else if (jcodingName.equals("UTF-32")) {
                // We use UTF_32BE because JCodings resolves UTF-32 to UTF32BEEncoding(dummy=true)
                // See org.jcodings.EncodingDB.dummy_unicode
                tsEncoding = TruffleString.Encoding.UTF_32BE;
            } else {
                tsEncoding = TruffleString.Encoding.fromJCodingName(jcodingName);
            }
            map[jcoding.getIndex()] = tsEncoding;
        }
        return map;
    }
}
