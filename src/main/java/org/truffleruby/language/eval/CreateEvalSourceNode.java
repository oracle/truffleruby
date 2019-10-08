/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.eval;

import java.util.Arrays;

import org.jcodings.Encoding;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.rope.CannotConvertBinaryRubyStringToJavaString;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.lexer.RubyLexer;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.Source;

public class CreateEvalSourceNode extends RubyBaseNode {

    @TruffleBoundary
    public RubySource createEvalSource(Rope code, String method, String file, int line) {
        final Rope sourceRope = createEvalRope(code, method, file, line);

        final String sourceString;
        try {
            sourceString = RopeOperations.decodeRope(sourceRope);
        } catch (CannotConvertBinaryRubyStringToJavaString e) {
            // In such a case, we have no way to build a Java String for the Truffle Source that
            // could accurately represent the source Rope, so we throw an error.
            final String message = file + ":" + line + ": cannot " + method +
                    "() a String with binary encoding, with no magic encoding comment and containing a non-US-ASCII character: \\x" +
                    String.format("%02X", e.getNonAsciiCharacter());
            throw new RaiseException(
                    getContext(),
                    coreExceptions().syntaxError(message, this, getEncapsulatingSourceSection()));
        }

        final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, sourceString, file).build();

        return new RubySource(source, sourceRope);
    }

    private static Rope createEvalRope(Rope source, String method, String file, int line) {
        final Encoding[] encoding = { source.getEncoding() };

        RubyLexer.parseMagicComment(source, (name, value) -> {
            if (RubyLexer.isMagicEncodingComment(name)) {
                encoding[0] = EncodingManager.getEncoding(value);
            }
        });

        if (source.getEncoding() != encoding[0]) {
            source = RopeOperations.withEncoding(source, encoding[0]);
        }

        // Do padding after magic comment detection
        return offsetSource(method, source, file, line);
    }

    private static Rope offsetSource(String method, Rope source, String file, int line) {
        // TODO CS 23-Apr-18 Truffle doesn't support line numbers starting at anything but 1
        if (line == 0) {
            // fine instead of warning because these seem common
            RubyLanguage.LOGGER.fine(() -> String.format(
                    "zero line number %s:%d not supported in #%s - will be reported as starting at 1",
                    file,
                    line,
                    method));
            return source;
        } else if (line < 1) {
            RubyLanguage.LOGGER.warning(
                    String.format(
                            "negative line number %s:%d not supported in #%s - will be reported as starting at 1",
                            file,
                            line,
                            method));
            return source;
        } else if (line > 1) {
            // fine instead of warning because we can simulate these
            RubyLanguage.LOGGER.fine(() -> String.format(
                    "offset line number %s:%d are simulated in #%s by adding blank lines",
                    file,
                    line,
                    method));
            if (!source.getEncoding().isAsciiCompatible()) {
                throw new UnsupportedOperationException("Cannot prepend newlines in an ASCII incompatible encoding");
            }
            final int n = line - 1;
            final byte[] bytes = new byte[n + source.byteLength()];
            Arrays.fill(bytes, 0, n, (byte) '\n');
            System.arraycopy(source.getBytes(), 0, bytes, n, source.byteLength());
            return RopeOperations.create(bytes, source.getEncoding(), source.getCodeRange());
        } else {
            return source;
        }
    }

}
