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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.source.Source;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.parser.RubySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * Loads normal Ruby source files from the file system.
 */
public class FileLoader {

    private final RubyContext context;

    public FileLoader(RubyContext context) {
        this.context = context;
    }

    @CompilerDirectives.TruffleBoundary
    public RubySource load(String feature) throws IOException {
        if (context.getOptions().LOG_LOAD) {
            RubyLanguage.LOGGER.info("loading " + feature);
        }

        return loadNoLogging(context, feature, context.getSourceLoader().isInternal(feature));
    }

    private static RubySource loadNoLogging(RubyContext context, String feature, boolean internal) throws IOException {
        SourceLoader.ensureReadable(context, feature);

        final File featureFile = new File(feature);

        final String mimeType;

        if (feature.toLowerCase().endsWith(RubyLanguage.CEXT_EXTENSION)) {
            mimeType = RubyLanguage.CEXT_MIME_TYPE;
        } else {
            // We need to assume all other files are Ruby, so the file type detection isn't enough
            mimeType = RubyLanguage.MIME_TYPE;
        }

        final String name;

        if (context != null && context.isPreInitializing()) {
            name = RubyLanguage.RUBY_HOME_SCHEME + Paths.get(context.getRubyHome()).relativize(Paths.get(feature));
        } else {
            name = feature;
        }

        final Rope sourceRope = readSourceRope(featureFile);

        final Source source = buildSource(
                Source.newBuilder(featureFile)
                        .name(name.intern())
                        .mimeType(mimeType), internal);

        return new RubySource(source, sourceRope);
    }

    private static Rope readSourceRope(File file) throws IOException {
        /*
         * We must read the file bytes ourselves - otherwise Truffle will read them, assume they're UTF-8, and we will
         * not be able to re-interpret the encoding later without the risk of the values being corrupted by being
         * passed through UTF-8.
         */

        final byte[] sourceBytes = Files.readAllBytes(file.toPath());
        return RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
    }

    private static <E extends Exception> Source buildSource(Source.Builder<E, RuntimeException, RuntimeException> builder, boolean internal) throws E {
        if (internal) {
            return builder.internal().build();
        } else {
            return builder.build();
        }
    }


}
