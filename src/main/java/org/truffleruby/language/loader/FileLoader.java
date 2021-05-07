/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import com.oracle.truffle.api.nodes.Node;
import org.graalvm.collections.Pair;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.source.Source;

/*
 * Loads normal Ruby source files from the file system.
 */
public class FileLoader {

    private final RubyContext context;
    private final RubyLanguage language;

    public FileLoader(RubyContext context, RubyLanguage language) {
        this.context = context;
        this.language = language;
    }

    public static void ensureReadable(RubyContext context, TruffleFile file, Node currentNode) {
        if (!file.exists()) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().loadError(
                            "No such file or directory -- " + file,
                            file.getPath(),
                            currentNode));
        }

        if (!file.isReadable()) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().loadError("Permission denied -- " + file, file.getPath(), currentNode));
        }
    }


    public Pair<Source, Rope> loadFile(String path) throws IOException {
        if (context.getOptions().LOG_LOAD) {
            RubyLanguage.LOGGER.info("loading " + path);
        }

        final TruffleFile file = getSafeTruffleFile(context, path);
        ensureReadable(context, file, null);

        /* We read the file's bytes ourselves because the lexer works on bytes and Truffle only gives us a CharSequence.
         * We could convert the CharSequence back to bytes, but that's more expensive than just reading the bytes once
         * and pass them down to the lexer and to the Source. */

        final byte[] sourceBytes = file.readAllBytes();
        final Rope sourceRope = RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        final Source source = buildSource(file, path, sourceRope, isInternal(path));
        return Pair.create(source, sourceRope);
    }

    static TruffleFile getSafeTruffleFile(RubyContext context, String path) {
        final Env env = context.getEnv();
        final TruffleFile file;
        try {
            file = env.getInternalTruffleFile(path).getCanonicalFile();
        } catch (IOException e) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().loadError("Failed to canonicalize -- " + path, path, null));
        }

        final TruffleFile home = context.getRubyHomeTruffleFile();
        if (file.startsWith(home) && isStdLibRubyOrCExtFile(home.relativize(file))) {
            return file;
        } else {
            try {
                return env.getPublicTruffleFile(path);
            } catch (SecurityException e) {
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().loadError(
                                "Permission denied (" + e.getMessage() + ") -- " + path,
                                path,
                                null));
            }
        }
    }

    private static boolean isStdLibRubyOrCExtFile(TruffleFile relativePathFromHome) {
        final String fileName = relativePathFromHome.getName();
        if (fileName == null) {
            return false;
        }

        final String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
        if (!lowerCaseFileName.endsWith(TruffleRuby.EXTENSION) &&
                !lowerCaseFileName.endsWith(RubyLanguage.CEXT_EXTENSION)) {
            return false;
        }

        return relativePathFromHome.startsWith("lib");
    }

    Source buildSource(TruffleFile file, String path, Rope sourceRope, boolean internal) {
        /* I'm not sure why we need to explicitly set a MIME type here - we say it's Ruby and this is the only and
         * default MIME type that Ruby supports.
         *
         * But if you remove setting the MIME type you get the following failures:
         *
         * - test/truffle/compiler/stf-optimises.sh (I think the value is different, not the compilation that fails) -
         * test/truffle/integration/tracing.sh (again, probably the values, and I'm not sure we were correct before,
         * it's just changed) */

        assert file.getPath().equals(path);

        final Source source = Source
                .newBuilder(TruffleRuby.LANGUAGE_ID, file)
                .canonicalizePath(false)
                .mimeType(TruffleRuby.MIME_TYPE)
                .content(RopeOperations.decodeOrEscapeBinaryRope(sourceRope))
                .internal(internal)
                .build();

        assert source.getPath().equals(path) : "Source#getPath() = " + source.getPath() + " is not the same as " + path;
        return source;
    }

    private boolean isInternal(String path) {
        final String canonicalPath;
        try {
            canonicalPath = new File(path).getCanonicalPath();
        } catch (IOException e) {
            return false;
        }

        if (canonicalPath.startsWith(language.coreLoadPath)) {
            return language.options.CORE_AS_INTERNAL;
        }

        // If the file is part of the standard library then we may consider it internal
        if (canonicalPath.startsWith(context.getRubyHome() + "/lib/") &&
                !canonicalPath.startsWith(context.getRubyHome() + "/lib/gems/")) {
            return language.options.STDLIB_AS_INTERNAL;
        } else {
            return false;
        }
    }

}
