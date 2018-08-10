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
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.aot.ParserCache;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.ast.RootParseNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

public class SourceLoader {

    public static final String RESOURCE_SCHEME = "resource:";
    public static final String RUBY_HOME_SCHEME = "rubyHome:";

    private final RubyContext context;

    private Source mainSource = null;
    private String mainSourceAbsolutePath = null;

    public SourceLoader(RubyContext context) {
        this.context = context;
    }

    /**
     * Returns the path of a Source. Returns the short path for the main script (the file argument
     * given to "ruby"). The path of eval(code, nil, filename) is just filename.
     */
    public String getPath(Source source) {
        final String name = source.getName();
        if (context.wasPreInitialized() && name.startsWith(RUBY_HOME_SCHEME)) {
            return context.getRubyHome() + "/" + name.substring(RUBY_HOME_SCHEME.length());
        } else {
            return name;
        }
    }

    /**
     * Returns the path of a Source. Returns the canonical path for the main script. Note however
     * that the path of eval(code, nil, filename) is just filename and might not be absolute.
     */
    public String getAbsolutePath(Source source) {
        if (source == mainSource) {
            return mainSourceAbsolutePath;
        } else {
            return getPath(source);
        }
    }

    @TruffleBoundary
    public String fileLine(SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final String path = getPath(section.getSource());

            if (section.isAvailable()) {
                return path + ":" + section.getStartLine();
            } else {
                return path;
            }
        }
    }

    @TruffleBoundary
    public RubySource loadMainEval() {
        final Source source = Source.newBuilder(context.getOptions().TO_EXECUTE).name("-e").mimeType(RubyLanguage.MIME_TYPE).build();
        return new RubySource(source);
    }

    @TruffleBoundary
    public RubySource loadMainStdin(RubyNode currentNode, String path) throws IOException {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        final byte[] buffer = new byte[4096];

        while (true) {
            final int read = System.in.read(buffer);
            if (read == -1) {
                break;
            }
            byteStream.write(buffer, 0, read);
        }

        final byte[] sourceBytes = xOptionStrip(currentNode, byteStream.toByteArray());
        final Rope sourceRope = RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        final Source source = Source.newBuilder(sourceRope.toString()).name(path).mimeType(RubyLanguage.MIME_TYPE).build();
        return new RubySource(source, sourceRope);
    }

    @TruffleBoundary
    public RubySource loadMainFile(RubyNode currentNode, String path) throws IOException {
        if (mainSourceAbsolutePath != null) {
            throw new UnsupportedOperationException("main file already loaded: " + mainSourceAbsolutePath);
        }

        ensureReadable(context, path);

        final File file = new File(path);

        final byte[] sourceBytes = xOptionStrip(currentNode, Files.readAllBytes(file.toPath()));
        final Rope sourceRope = RopeOperations.create(sourceBytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);

        mainSource = Source.newBuilder(file).name(path).content(sourceRope.toString()).mimeType(RubyLanguage.MIME_TYPE).build();

        mainSourceAbsolutePath = file.getCanonicalPath();
        return new RubySource(mainSource, sourceRope);
    }

    private byte[] xOptionStrip(RubyNode currentNode, byte[] sourceBytes) throws IOException {
        boolean lookForRubyShebang = isCurrentLineShebang(sourceBytes) ||
                context.getOptions().IGNORE_LINES_BEFORE_RUBY_SHEBANG;

        ByteArrayOutputStream content = new ByteArrayOutputStream();

        int n = 0;

        if (lookForRubyShebang) {
            while (true) {
                if (n == sourceBytes.length) {
                    throw new RaiseException(context, context.getCoreExceptions().loadError(
                            "no Ruby script found in input",
                            "",
                            currentNode));
                }

                final int startOfLine = n;

                while (n < sourceBytes.length && sourceBytes[n] != '\n') {
                    n++;
                }

                if (n < sourceBytes.length && sourceBytes[n] == '\n') {
                    n++;
                }

                final byte[] lineBytes = Arrays.copyOfRange(sourceBytes, startOfLine, n);
                final String line = new String(lineBytes, StandardCharsets.US_ASCII);

                final boolean rubyShebang = line.startsWith("#!") && line.contains("ruby");
                if (rubyShebang) {
                    content.write(lineBytes);
                    break;
                } else {
                    content.write("# line ignored by Ruby:".getBytes(StandardCharsets.US_ASCII)); // prefix with a comment so it's ignored by parser
                    content.write(lineBytes);
                }
            }
        }

        content.write(sourceBytes, n, sourceBytes.length - n);

        return content.toByteArray();
    }

    private boolean isCurrentLineShebang(byte[] bytes) {
        return bytes.length >= 2 && bytes[0] == '#' && bytes[1] == '!';
    }

    @TruffleBoundary
    public RubySource loadCoreFile(String feature) throws IOException {
        if (feature.startsWith(RESOURCE_SCHEME)) {
            if (TruffleOptions.AOT || ParserCache.INSTANCE != null) {
                final RootParseNode rootParseNode = ParserCache.INSTANCE.get(feature);
                return new RubySource(rootParseNode.getSource());
            } else {
                return loadResource(feature, isInternal(feature));
            }
        } else {
            return load(feature);
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
            name = RUBY_HOME_SCHEME + Paths.get(context.getRubyHome()).relativize(Paths.get(feature));
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

    private boolean isInternal(String canonicalPath) {
        if (canonicalPath.startsWith(context.getCoreLibrary().getCoreLoadPath())) {
            return context.getOptions().CORE_AS_INTERNAL;
        }

        if (canonicalPath.startsWith(context.getRubyHome())) {
            return context.getOptions().STDLIB_AS_INTERNAL;
        }

        return false;
    }

    public static RubySource loadResource(String path, boolean internal) throws IOException {
        assert path.startsWith(RESOURCE_SCHEME);

        if (!path.toLowerCase(Locale.ENGLISH).endsWith(".rb")) {
            throw new FileNotFoundException(path);
        }

        final Class<?> relativeClass = RubyContext.class;
        final Path relativePath = Paths.get(path.substring(RESOURCE_SCHEME.length()));

        final String normalizedPath = StringUtils.replace(relativePath.normalize().toString(), '\\', '/');
        final InputStream stream = relativeClass.getResourceAsStream(normalizedPath);

        if (stream == null) {
            throw new FileNotFoundException(path);
        }

        final InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        final Source source = buildSource(
                Source.newBuilder(reader).name(path).mimeType(RubyLanguage.MIME_TYPE), internal);
        return new RubySource(source);
    }

    private static <E extends Exception> Source buildSource(Source.Builder<E, RuntimeException, RuntimeException> builder, boolean internal) throws E {
        if (internal) {
            return builder.internal().build();
        } else {
            return builder.build();
        }
    }

    private static void ensureReadable(RubyContext context, String path) {
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
