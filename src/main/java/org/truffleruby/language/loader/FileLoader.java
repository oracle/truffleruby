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
import org.truffleruby.language.control.RaiseException;
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

    public void ensureReadable(String path) {
        if (context == null) {
            // Ignore during pre-initialisation
            return;
        }

        final File file = new File(path);

        if (!file.exists()) {
            throw new RaiseException(context, context.getCoreExceptions().loadError("No such file or directory -- " + path, path, null));
        }

        if (!file.canRead()) {
            throw new RaiseException(context, context.getCoreExceptions().loadError("Permission denied -- " + path, path, null));
        }
    }

    public RubySource loadFile(String path) throws IOException {
        if (context.getOptions().LOG_LOAD) {
            RubyLanguage.LOGGER.info("loading " + path);
        }

        ensureReadable(path);

        final String name;

        if (context != null && context.isPreInitializing()) {
            name = RubyLanguage.RUBY_HOME_SCHEME + Paths.get(context.getRubyHome()).relativize(Paths.get(path));
        } else {
            name = path;
        }

        /*
         * We must read the file bytes ourselves - otherwise Truffle will read them, assume they're UTF-8, and we will
         * not be able to re-interpret the encoding later without the risk of the values being corrupted by being
         * passed through UTF-8.
         */

        final byte[] sourceBytes = Files.readAllBytes(Paths.get(path));
        final Rope sourceRope = RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        final String language;
        final String mimeType;

        if (path.toLowerCase(Locale.ENGLISH).endsWith(RubyLanguage.CEXT_EXTENSION)) {
            language = TruffleRuby.LLVM_ID;
            mimeType = RubyLanguage.CEXT_MIME_TYPE;
        } else {
            // We need to assume all other files are Ruby, so the file type detection isn't enough
            language = TruffleRuby.LANGUAGE_ID;
            mimeType = TruffleRuby.MIME_TYPE;
        }

        // We set an explicit MIME type because LLVM does not have a default one

        final Source source = Source.newBuilder(language, sourceRope.toString(), name)
                .mimeType(mimeType)
                .internal(isInternal(path))
                .build();

        return new RubySource(source, sourceRope);
    }

    private boolean isInternal(String path) {
        // If the file is part of the standard library then we may consider it internal

        final String canonicalPath;

        try {
            canonicalPath = new File(path).getCanonicalPath();
        } catch (IOException e) {
            return false;
        }

        if (canonicalPath.startsWith(context.getRubyHome() + "/lib/") &&
                !canonicalPath.startsWith(context.getRubyHome() + "/lib/ruby/gems/")) {
            return context.getOptions().STDLIB_AS_INTERNAL;
        } else {
            return false;
        }
    }

}
