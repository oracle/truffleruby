/*
 * Copyright (c) 2013, 2025 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.strings.AbstractTruffleString;
import org.graalvm.shadowed.org.jcodings.Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.TStringUtils;

/** Helpers for {@link java.lang.String} */
public abstract class StringOperations {

    public static RubyString createUTF8String(RubyContext context, RubyLanguage language, String string) {
        final RubyString instance = new RubyString(
                context.getCoreLibrary().stringClass,
                language.stringShape,
                false,
                TStringUtils.utf8TString(string),
                Encodings.UTF_8);

        return instance;
    }

    public static RubyString createUTF8String(RubyContext context, RubyLanguage language,
            AbstractTruffleString string) {
        final RubyString instance = new RubyString(
                context.getCoreLibrary().stringClass,
                language.stringShape,
                false,
                string,
                Encodings.UTF_8);

        return instance;
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

    /** Prefer this to {@code getBytes(StandardCharsets.US_ASCII)} */
    public static byte[] encodeAsciiBytes(String value) {
        assert isAsciiOnly(value) : "String contained non ascii characters \"" + value + "\"";

        final byte[] bytes = new byte[value.length()];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) value.charAt(i);
        }

        return bytes;
    }

}
