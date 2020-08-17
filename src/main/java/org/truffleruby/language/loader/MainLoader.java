/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

/*
 * Loads the main script, whether it comes from an argument, standard in, or a file.
 */
public class MainLoader {

    private final RubyContext context;

    public MainLoader(RubyContext context) {
        this.context = context;
    }

    public RubySource loadFromCommandLineArgument(String code) {
        final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, code, "-e").build();
        return new RubySource(source, "-e");
    }

    public RubySource loadFromStandardIn(Node currentNode, String path) throws IOException {
        byte[] sourceBytes = readAllOfStandardIn();
        final Rope sourceRope = transformScript(currentNode, path, sourceBytes);

        final Source source = Source.newBuilder(
                TruffleRuby.LANGUAGE_ID,
                RopeOperations.decodeOrEscapeBinaryRope(sourceRope),
                path).build();
        return new RubySource(source, path, sourceRope);
    }

    private Rope transformScript(Node currentNode, String path, byte[] sourceBytes) {
        final EmbeddedScript embeddedScript = new EmbeddedScript(context);

        if (embeddedScript.shouldTransform(sourceBytes)) {
            sourceBytes = embeddedScript.transformForExecution(currentNode, sourceBytes, path);
        }

        return RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
    }

    private byte[] readAllOfStandardIn() throws IOException {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(4096);

        final byte[] buffer = new byte[4096];

        while (true) {
            final int read = System.in.read(buffer);

            if (read == -1) {
                break;
            }

            byteStream.write(buffer, 0, read);
        }

        return byteStream.toByteArray();
    }

    public RubySource loadFromFile(Env env, Node currentNode, String mainPath) throws IOException {
        final FileLoader fileLoader = new FileLoader(context);

        final TruffleFile file = env.getPublicTruffleFile(mainPath);
        FileLoader.ensureReadable(context, file, currentNode);

        /* We read the file's bytes ourselves because the lexer works on bytes and Truffle only gives us a CharSequence.
         * We could convert the CharSequence back to bytes, but that's more expensive than just reading the bytes once
         * and pass them down to the lexer and to the Source. */

        byte[] sourceBytes = file.readAllBytes();
        final Rope sourceRope = transformScript(currentNode, mainPath, sourceBytes);

        final Source mainSource = fileLoader.buildSource(file, mainPath, sourceRope, false);

        return new RubySource(mainSource, mainPath, sourceRope);
    }

}
