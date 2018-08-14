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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.RubySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SourceLoader {

    private final RubyContext context;

    public SourceLoader(RubyContext context) {
        this.context = context;
    }

    @TruffleBoundary
    public String fileLine(SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final String path = context.getPath(section.getSource());

            if (section.isAvailable()) {
                return path + ":" + section.getStartLine();
            } else {
                return path;
            }
        }
    }

    @TruffleBoundary
    public RubySource load(String feature) throws IOException {
        if (context.getOptions().LOG_LOAD) {
            RubyLanguage.LOGGER.info("loading " + feature);
        }

        return loadNoLogging(context, feature, isInternal(feature));
    }

    public static RubySource loadNoLogging(RubyContext context, String feature, boolean internal) throws IOException {
        ensureReadable(context, feature);

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

    public boolean isInternal(String canonicalPath) {
        if (canonicalPath.startsWith(context.getCoreLibrary().getCoreLoadPath())) {
            return context.getOptions().CORE_AS_INTERNAL;
        }

        if (canonicalPath.startsWith(context.getRubyHome() + "/lib/") &&
                !canonicalPath.startsWith(context.getRubyHome() + "/lib/ruby/gems/")) {
            return context.getOptions().STDLIB_AS_INTERNAL;
        }

        return false;
    }

    private static <E extends Exception> Source buildSource(Source.Builder<E, RuntimeException, RuntimeException> builder, boolean internal) throws E {
        if (internal) {
            return builder.internal().build();
        } else {
            return builder.build();
        }
    }

    public static void ensureReadable(RubyContext context, String path) {
        if (context != null) {
            final File file = new File(path);
            if (!file.exists()) {
                throw new RaiseException(context, context.getCoreExceptions().loadError("No such file or directory -- " + path, path, null));
            }

            if (!file.canRead()) {
                throw new RaiseException(context, context.getCoreExceptions().loadError("Permission denied -- " + path, path, null));
            }
        }
    }

}
