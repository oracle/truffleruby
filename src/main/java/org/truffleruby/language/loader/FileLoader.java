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
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.TruffleRuby;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/*
 * Loads normal Ruby source files from the file system.
 */
public class FileLoader {

    private final RubyContext context;

    public FileLoader(RubyContext context) {
        this.context = context;
    }

    public RubySource loadFile(String feature) throws IOException {
        if (context.getOptions().LOG_LOAD) {
            RubyLanguage.LOGGER.info("loading " + feature);
        }

        SourceLoader.ensureReadable(context, feature);

        final String language;
        final String mimeType;

        if (feature.toLowerCase(Locale.ENGLISH).endsWith(RubyLanguage.CEXT_EXTENSION)) {
            language = TruffleRuby.LLVM_ID;
            mimeType = RubyLanguage.CEXT_MIME_TYPE;
        } else {
            // We need to assume all other files are Ruby, so the file type detection isn't enough
            language = TruffleRuby.LANGUAGE_ID;
            mimeType = RubyLanguage.MIME_TYPE;
        }

        final String name;

        if (context != null && context.isPreInitializing()) {
            name = RubyLanguage.RUBY_HOME_SCHEME + Paths.get(context.getRubyHome()).relativize(Paths.get(feature));
        } else {
            name = feature;
        }

        /*
         * We must read the file bytes ourselves - otherwise Truffle will read them, assume they're UTF-8, and we will
         * not be able to re-interpret the encoding later without the risk of the values being corrupted by being
         * passed through UTF-8.
         */

        final byte[] sourceBytes = Files.readAllBytes(Paths.get(feature));
        final Rope sourceRope = RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);

        Source.Builder<IOException, RuntimeException, RuntimeException> builder
                = Source.newBuilder(new File(feature))
                    .language(language)
                    .name(name.intern())
                    .mimeType(mimeType);

        final Source source;

        if (context.getSourceLoader().isInternal(feature)) {
            source = builder.internal().build();
        } else {
            source = builder.build();
        }

        return new RubySource(source, sourceRope);
    }

}
