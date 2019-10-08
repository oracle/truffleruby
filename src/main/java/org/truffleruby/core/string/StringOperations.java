/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
/*
 * Copyright (c) 2013, 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Contains code modified from JRuby's RubyString.java
 *
 * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2002-2006 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2004 David Corbin <dcorbin@users.sourceforge.net>
 * Copyright (C) 2005 Tim Azzopardi <tim@tigerfive.com>
 * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
 * Copyright (C) 2006 Ola Bini <ola@ologix.com>
 * Copyright (C) 2007 Nick Sieger <nicksieger@gmail.com>
 *
 */
package org.truffleruby.core.string;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class StringOperations {

    public static DynamicObject createString(RubyContext context, Rope rope) {
        return context.getCoreLibrary().getStringFactory().newInstance(Layouts.STRING.build(false, false, rope));
    }

    public static DynamicObject createFrozenString(RubyContext context, Rope rope) {
        return context.getCoreLibrary().getStringFactory().newInstance(Layouts.STRING.build(true, false, rope));
    }

    public static String getString(DynamicObject string) {
        return RopeOperations.decodeRope(StringOperations.rope(string));
    }

    public static int clampExclusiveIndex(DynamicObject string, int index) {
        // TODO (nirvdrum 21-Jan-16): Verify this is supposed to be the byteLength and not the characterLength.
        return ArrayOperations.clampExclusiveIndex(StringOperations.rope(string).byteLength(), index);
    }

    @TruffleBoundary
    public static byte[] encodeBytes(String value, Encoding encoding) {
        // Taken from org.jruby.RubyString#encodeByteList.

        if (encoding == ASCIIEncoding.INSTANCE && !isAsciiOnly(value)) {
            throw new UnsupportedOperationException(
                    StringUtils.format(
                            "Can't convert Java String (%s) to Ruby BINARY String because it contains non-ASCII characters",
                            value));
        }

        Charset charset = encoding.getCharset();

        if (charset == null) {
            throw new UnsupportedOperationException("Cannot find Charset to encode " + value + " with " + encoding);
        }

        final ByteBuffer buffer = charset.encode(CharBuffer.wrap(value));
        final byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        return bytes;
    }

    public static Rope encodeRope(String value, Encoding encoding, CodeRange codeRange) {
        if (codeRange == CodeRange.CR_7BIT) {
            return RopeOperations.encodeAscii(value, encoding);
        }

        final byte[] bytes = encodeBytes(value, encoding);

        return RopeOperations.create(bytes, encoding, codeRange);
    }

    public static Rope encodeRope(String value, Encoding encoding) {
        return encodeRope(value, encoding, CodeRange.CR_UNKNOWN);
    }

    public static Rope rope(DynamicObject string) {
        return Layouts.STRING.getRope(string);
    }

    public static void setRope(DynamicObject string, Rope rope) {
        Layouts.STRING.setRope(string, rope);
    }

    public static Encoding encoding(DynamicObject string) {
        return rope(string).getEncoding();
    }

    public static boolean isAsciiOnly(String string) {
        for (int i = 0; i < string.length(); i++) {
            int c = string.charAt(i);
            if (!Encoding.isAscii(c)) {
                return false;
            }
        }
        return true;
    }
}
