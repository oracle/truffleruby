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
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.ImmutableRubyString;

public class Encodings {

    public static final int INITIAL_NUMBER_OF_ENCODINGS = EncodingDB.getEncodings().size();
    public static final RubyEncoding US_ASCII = initializeUsAscii();
    public static final RubyEncoding[] BUILT_IN_ENCODINGS = initializeRubyEncodings();
    public static final RubyEncoding BINARY = BUILT_IN_ENCODINGS[ASCIIEncoding.INSTANCE.getIndex()];
    public static final RubyEncoding UTF_8 = BUILT_IN_ENCODINGS[UTF8Encoding.INSTANCE.getIndex()];

    public Encodings() {
        initializeRubyEncodings();
    }

    private static RubyEncoding initializeUsAscii() {
        final Encoding encoding = USASCIIEncoding.INSTANCE;
        return new RubyEncoding(encoding, new String(encoding.getName()), encoding.getIndex());
    }

    private static RubyEncoding[] initializeRubyEncodings() {
        final RubyEncoding[] encodings = new RubyEncoding[INITIAL_NUMBER_OF_ENCODINGS];
        final CaseInsensitiveBytesHash<EncodingDB.Entry>.CaseInsensitiveBytesHashEntryIterator hei = EncodingDB
                .getEncodings()
                .entryIterator();
        while (hei.hasNext()) {
            final CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry> e = hei.next();
            final EncodingDB.Entry encodingEntry = e.value;
            if (encodingEntry.getEncoding() == USASCIIEncoding.INSTANCE) {
                encodings[encodingEntry.getEncoding().getIndex()] = US_ASCII;
                continue;
            }
            assert e.p == 0 : "Ropes can't be created with non-zero offset: " + e.p;
            assert e.end == e.bytes.length : "Ropes must have the same exact length as the name array (len = " + e.end +
                    "; name.length = " + e.bytes.length + ")";
            final ImmutableRubyString name = new ImmutableRubyString(
                    RopeConstants.ROPE_CONSTANTS.get(new String(e.bytes)),
                    US_ASCII);
            final RubyEncoding rubyEncoding = newRubyEncoding(
                    name,
                    encodingEntry.getEncoding(),
                    encodingEntry.getEncoding().getIndex(),
                    encodingEntry.getEncoding().isDummy());
            encodings[encodingEntry.getEncoding().getIndex()] = rubyEncoding;
        }
        return encodings;
    }

    @TruffleBoundary
    public static RubyEncoding newRubyEncoding(RubyLanguage language, Encoding encoding, int index, byte[] name, int p,
            int end, boolean dummy) {
        assert p == 0 : "Ropes can't be created with non-zero offset: " + p;
        assert end == name.length : "Ropes must have the same exact length as the name array (len = " + end +
                "; name.length = " + name.length + ")";

        final Rope rope = RopeOperations.create(name, USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        final ImmutableRubyString string = language.getFrozenStringLiteral(rope);

        return newRubyEncoding(string, encoding, index, dummy);
    }

    @TruffleBoundary
    private static RubyEncoding newRubyEncoding(ImmutableRubyString name, Encoding encoding, int index, boolean dummy) {
        return new RubyEncoding(encoding, name, index, dummy);
    }

    public static RubyEncoding getBuiltInEncoding(int index) {
        return BUILT_IN_ENCODINGS[index];
    }

}
