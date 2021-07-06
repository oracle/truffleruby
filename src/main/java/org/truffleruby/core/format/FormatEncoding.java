/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format;

import com.oracle.truffle.api.nodes.Node;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.language.control.RaiseException;

public enum FormatEncoding {

    DEFAULT(Encodings.BINARY),
    ASCII_8BIT(Encodings.BINARY),
    US_ASCII(Encodings.US_ASCII),
    UTF_8(Encodings.UTF_8);

    private final RubyEncoding encoding;

    FormatEncoding(RubyEncoding encoding) {
        this.encoding = encoding;
    }

    public RubyEncoding getEncoding() {
        return encoding;
    }

    public RubyEncoding getEncodingForLength(int length) {
        if (length == 0) {
            return Encodings.US_ASCII;
        } else {
            return encoding;
        }
    }

    public static FormatEncoding find(Encoding encoding, Node currentNode) {
        if (encoding == ASCIIEncoding.INSTANCE) {
            return ASCII_8BIT;
        }

        if (encoding == USASCIIEncoding.INSTANCE) {
            return US_ASCII;
        }

        if (encoding == UTF8Encoding.INSTANCE) {
            return UTF_8;
        }

        // TODO (kjmenard 17-Oct-18): This entire enum needs to be rethought since a format string can take on any encoding, not just the 3 codified here.
        RubyContext context = RubyLanguage.getCurrentContext();
        throw new RaiseException(
                context,
                context.getCoreExceptions().runtimeError("Can't find format encoding for " + encoding, currentNode));
    }

    /** Given the current encoding for a pack string, and something that requires another encoding, give us the encoding
     * that we should use for the result of pack. */
    public FormatEncoding unifyWith(FormatEncoding other) {
        if (this == DEFAULT) {
            return other;
        }

        if (other == DEFAULT) {
            return this;
        }

        switch (other) {
            case ASCII_8BIT:
            case US_ASCII:
                return ASCII_8BIT;
            case UTF_8:
                switch (this) {
                    case ASCII_8BIT:
                    case US_ASCII:
                        return ASCII_8BIT;
                    case UTF_8:
                        return UTF_8;
                    default:
                        throw CompilerDirectives.shouldNotReachHere();
                }
            default:
                throw CompilerDirectives.shouldNotReachHere();
        }
    }

}
