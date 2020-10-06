/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.eval;

import org.jcodings.Encoding;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.rope.CannotConvertBinaryRubyStringToJavaString;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.lexer.RubyLexer;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.Source;

public class CreateEvalSourceNode extends RubyContextNode {

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

        final RubySource rubySource = new RubySource(source, file, sourceRope, true, line - 1);

        getContext().getSourceLineOffsets().put(source, line - 1);
        return rubySource;
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

        return source;
    }

}
