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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
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
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.FrozenStringLiterals;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.TStringConstants;

public class Encodings {

    public static final int INITIAL_NUMBER_OF_ENCODINGS = EncodingDB.getEncodings().size();
    public static final RubyEncoding US_ASCII = initializeUsAscii();
    private static final RubyEncoding[] BUILT_IN_ENCODINGS = initializeRubyEncodings();

    public static final RubyEncoding BINARY = BUILT_IN_ENCODINGS[ASCIIEncoding.INSTANCE.getIndex()];
    public static final RubyEncoding UTF_8 = BUILT_IN_ENCODINGS[UTF8Encoding.INSTANCE.getIndex()];
    public static final RubyEncoding UTF16LE = BUILT_IN_ENCODINGS[UTF16LEEncoding.INSTANCE.getIndex()];
    public static final RubyEncoding UTF16BE = BUILT_IN_ENCODINGS[UTF16BEEncoding.INSTANCE.getIndex()];
    public static final RubyEncoding UTF32LE = BUILT_IN_ENCODINGS[UTF32LEEncoding.INSTANCE.getIndex()];
    public static final RubyEncoding UTF32BE = BUILT_IN_ENCODINGS[UTF32BEEncoding.INSTANCE.getIndex()];
    public static final RubyEncoding ISO_8859_1 = BUILT_IN_ENCODINGS[ISO8859_1Encoding.INSTANCE.getIndex()];
    public static final RubyEncoding UTF16_DUMMY = BUILT_IN_ENCODINGS[EncodingDB
            .getEncodings()
            .get(RopeOperations.encodeAsciiBytes("UTF-16"))
            .getEncoding()
            .getIndex()];
    public static final RubyEncoding UTF32_DUMMY = BUILT_IN_ENCODINGS[EncodingDB
            .getEncodings()
            .get(RopeOperations.encodeAsciiBytes("UTF-32"))
            .getEncoding()
            .getIndex()];

    static final Encoding DUMMY_ENCODING_BASE = createDummyEncoding();

    public Encodings() {
    }

    private static RubyEncoding initializeUsAscii() {
        final Encoding encoding = USASCIIEncoding.INSTANCE;
        return new RubyEncoding(encoding.getIndex());
    }

    private static RubyEncoding[] initializeRubyEncodings() {
        final RubyEncoding[] encodings = new RubyEncoding[INITIAL_NUMBER_OF_ENCODINGS];
        for (var entry : EncodingDB.getEncodings()) {
            final Encoding encoding = entry.getEncoding();

            final RubyEncoding rubyEncoding;
            if (encoding == USASCIIEncoding.INSTANCE) {
                rubyEncoding = US_ASCII;
            } else {
                final ImmutableRubyString name = FrozenStringLiterals.createStringAndCacheLater(
                        TStringConstants.TSTRING_CONSTANTS.get(encoding.toString()),
                        RopeConstants.ROPE_CONSTANTS.get(encoding.toString()),
                        US_ASCII);
                rubyEncoding = new RubyEncoding(encoding, name, encoding.getIndex());
            }
            encodings[encoding.getIndex()] = rubyEncoding;
        }
        return encodings;
    }

    private static Encoding createDummyEncoding() {
        final EncodingDB.Entry entry = EncodingDB.dummy("TRUFFLERUBY_DUMMY_ENCODING".getBytes());
        return entry.getEncoding();
    }

    @TruffleBoundary
    public static RubyEncoding newRubyEncoding(RubyLanguage language, Encoding encoding, int index, byte[] name) {
        var tstring = TStringUtils.fromByteArray(name, Encodings.US_ASCII);
        final ImmutableRubyString string = language.getFrozenStringLiteral(tstring, Encodings.US_ASCII);

        return new RubyEncoding(encoding, string, index);
    }

    /** Should only be used when there is no other way, because this will ignore replicated and dummy encodings */
    public static RubyEncoding getBuiltInEncoding(Encoding jcoding) {
        var rubyEncoding = BUILT_IN_ENCODINGS[jcoding.getIndex()];
        assert rubyEncoding.jcoding == jcoding;
        return rubyEncoding;
    }

}
