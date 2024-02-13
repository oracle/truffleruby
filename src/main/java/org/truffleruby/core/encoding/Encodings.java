/*
 * Copyright (c) 2014, 2024 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.ISO8859_1Encoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF16BEEncoding;
import org.jcodings.specific.UTF16LEEncoding;
import org.jcodings.specific.UTF32BEEncoding;
import org.jcodings.specific.UTF32LEEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.FrozenStringLiterals;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.TStringConstants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Encodings {

    public static final int INITIAL_NUMBER_OF_ENCODINGS = EncodingDB.getEncodings().size();
    public static final int MAX_NUMBER_OF_ENCODINGS = 256;
    public static final int US_ASCII_INDEX = 0;
    public static final RubyEncoding US_ASCII = initializeUsAscii();
    static final RubyEncoding[] BUILT_IN_ENCODINGS = initializeRubyEncodings();
    private static final RubyEncoding[] BUILT_IN_ENCODINGS_BY_JCODING_INDEX = initializeBuiltinEncodingsByJCodingIndex();

    public static final RubyEncoding BINARY = getBuiltInEncoding(ASCIIEncoding.INSTANCE);
    public static final RubyEncoding UTF_8 = getBuiltInEncoding(UTF8Encoding.INSTANCE);
    public static final RubyEncoding UTF16LE = getBuiltInEncoding(UTF16LEEncoding.INSTANCE);
    public static final RubyEncoding UTF16BE = getBuiltInEncoding(UTF16BEEncoding.INSTANCE);
    public static final RubyEncoding UTF32LE = getBuiltInEncoding(UTF32LEEncoding.INSTANCE);
    public static final RubyEncoding UTF32BE = getBuiltInEncoding(UTF32BEEncoding.INSTANCE);
    public static final RubyEncoding ISO_8859_1 = getBuiltInEncoding(ISO8859_1Encoding.INSTANCE);
    public static final RubyEncoding UTF16_DUMMY = getBuiltInEncoding(
            EncodingDB.getEncodings().get(StringOperations.encodeAsciiBytes("UTF-16")).getEncoding());
    public static final RubyEncoding UTF32_DUMMY = getBuiltInEncoding(
            EncodingDB.getEncodings().get(StringOperations.encodeAsciiBytes("UTF-32")).getEncoding());

    /** On Linux and macOS the filesystem encoding is always UTF-8 */
    public static final RubyEncoding FILESYSTEM = UTF_8;
    public static final Charset FILESYSTEM_CHARSET = StandardCharsets.UTF_8;

    static final Encoding DUMMY_ENCODING_BASE = createDummyEncoding();

    public Encodings() {
    }

    private static RubyEncoding initializeUsAscii() {
        return new RubyEncoding(US_ASCII_INDEX);
    }

    private static RubyEncoding[] initializeRubyEncodings() {
        final RubyEncoding[] encodings = new RubyEncoding[INITIAL_NUMBER_OF_ENCODINGS];

        int index = US_ASCII_INDEX + 1;
        for (var entry : EncodingDB.getEncodings()) {
            final Encoding encoding = entry.getEncoding();

            if (encoding == USASCIIEncoding.INSTANCE) {
                encodings[US_ASCII_INDEX] = US_ASCII;
            } else {
                TruffleString tstring = TStringConstants.TSTRING_CONSTANTS.get(encoding.toString());
                if (tstring == null) {
                    throw CompilerDirectives.shouldNotReachHere("no TStringConstants for " + encoding);
                }
                final ImmutableRubyString name = FrozenStringLiterals.createStringAndCacheLater(tstring, US_ASCII);
                var rubyEncoding = new RubyEncoding(encoding, name, index);
                encodings[index] = rubyEncoding;
                index++;
            }
        }

        assert index == EncodingDB.getEncodings().size();
        return encodings;
    }

    private static Encoding createDummyEncoding() {
        final EncodingDB.Entry entry = EncodingDB
                .dummy(StringOperations.encodeAsciiBytes("TRUFFLERUBY_DUMMY_ENCODING"));
        return entry.getEncoding();
    }

    @TruffleBoundary
    public static RubyEncoding newRubyEncoding(RubyLanguage language, Encoding encoding, int index, byte[] name) {
        var tstring = TStringUtils.fromByteArray(name, Encodings.US_ASCII);
        final ImmutableRubyString string = language.getFrozenStringLiteral(tstring, Encodings.US_ASCII);

        return new RubyEncoding(encoding, string, index);
    }

    public static RubyEncoding[] initializeBuiltinEncodingsByJCodingIndex() {
        final RubyEncoding[] encodings = new RubyEncoding[INITIAL_NUMBER_OF_ENCODINGS];
        for (RubyEncoding encoding : BUILT_IN_ENCODINGS) {
            // This and the usage in getBuiltInEncoding() below should be the only usages of org.jcodings.Encoding#getIndex().
            // That index is not deterministic and depends on classloading, so use it as little as possible.
            encodings[encoding.jcoding.getIndex()] = encoding;
        }
        return encodings;
    }

    /** Should only be used when there is no other way, because this will ignore replicated and dummy encodings */
    public static RubyEncoding getBuiltInEncoding(Encoding jcoding) {
        var rubyEncoding = BUILT_IN_ENCODINGS_BY_JCODING_INDEX[jcoding.getIndex()];
        assert rubyEncoding.jcoding == jcoding;
        return rubyEncoding;
    }

    /** Should only be used when there is no other way, because this will ignore replicated and dummy encodings */
    public static RubyEncoding getBuiltInEncoding(String encodingName) {
        byte[] encodingNameBytes = encodingName.getBytes(StandardCharsets.ISO_8859_1);
        var entry = EncodingDB.getEncodings().get(encodingNameBytes);
        if (entry != null) {
            var jcoding = entry.getEncoding();
            return getBuiltInEncoding(jcoding);
        } else {
            throw CompilerDirectives.shouldNotReachHere("Unknown encoding: " + encodingName);
        }
    }

}
