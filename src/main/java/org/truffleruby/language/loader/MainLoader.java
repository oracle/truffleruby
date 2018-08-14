/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import com.oracle.truffle.api.source.Source;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.RubyNode;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.TruffleRuby;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * Loads the main script, whether it comes from an argument, standard in, or a file.
 */
public class MainLoader {

    private final RubyContext context;

    public MainLoader(RubyContext context) {
        this.context = context;
    }

    public RubySource loadFromCommandLineArgument() {
        final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, context.getOptions().TO_EXECUTE, "-e").build();
        return new RubySource(source);
    }

    public RubySource loadFromStandardIn(RubyNode currentNode, String path) throws IOException {
        byte[] sourceBytes = readAllOfStandardIn();

        final EmbeddedScript embeddedScript = new EmbeddedScript(context);

        if (embeddedScript.shouldTransform(sourceBytes)) {
            sourceBytes = embeddedScript.transformForExecution(currentNode, sourceBytes, path);
        }

        final Rope sourceRope = RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, sourceRope.toString(), path).build();
        return new RubySource(source, sourceRope);
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

    public RubySource loadFromFile(RubyNode currentNode, String path) throws IOException {
        SourceLoader.ensureReadable(context, path);

        /*
         * We must read the file bytes ourselves - otherwise Truffle will read them, assume they're UTF-8, and we will
         * not be able to re-interpret the encoding later without the risk of the values being corrupted by being
         * passed through UTF-8.
         */

        byte[] sourceBytes = Files.readAllBytes(Paths.get(path));

        final EmbeddedScript embeddedScript = new EmbeddedScript(context);

        if (embeddedScript.shouldTransform(sourceBytes)) {
            sourceBytes = embeddedScript.transformForExecution(currentNode, sourceBytes, path);
        }

        final Rope sourceRope = RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);

        final File file = new File(path);
        final Source mainSource = Source.newBuilder(file).name(path).content(sourceRope.toString()).mimeType(RubyLanguage.MIME_TYPE).build();
        final String mainSourceAbsolutePath = file.getAbsolutePath();

        context.setMainSources(mainSource, mainSourceAbsolutePath);

        return new RubySource(mainSource, sourceRope);
    }

}
