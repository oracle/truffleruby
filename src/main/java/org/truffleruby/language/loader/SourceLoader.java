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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.aot.ParserCache;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.ast.RootParseNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        final Source source = Source.newBuilder(xOptionStrip(currentNode,
                new InputStreamReader(System.in))).name(path).mimeType(RubyLanguage.MIME_TYPE).build();
        return new RubySource(source);
    }

    @TruffleBoundary
    public RubySource loadMainFile(RubyNode currentNode, String path) throws IOException {
        if (mainSourceAbsolutePath != null) {
            throw new UnsupportedOperationException("main file already loaded: " + mainSourceAbsolutePath);
        }

        ensureReadable(context, path);

        final File file = new File(path);
        final String content = xOptionStrip(currentNode, new FileReader(file));
        mainSource = Source.newBuilder(file).name(path).content(content).mimeType(RubyLanguage.MIME_TYPE).build();
        mainSourceAbsolutePath = file.getCanonicalPath();
        return new RubySource(mainSource);
    }

    private String xOptionStrip(RubyNode currentNode, Reader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {

            boolean lookForRubyShebang = isCurrentLineShebang(bufferedReader) ||
                    context.getOptions().IGNORE_LINES_BEFORE_RUBY_SHEBANG;

            if (lookForRubyShebang) {
                while (true) {
                    final String line = bufferedReader.readLine();
                    if (line == null) {
                        throw new RaiseException(context, context.getCoreExceptions().loadError(
                                "no Ruby script found in input",
                                "",
                                currentNode));
                    }

                    final boolean rubyShebang = line.startsWith("#!") && line.contains("ruby");
                    if (rubyShebang) {
                        content.append(line);
                        content.append("\n");
                        break;
                    } else {
                        content.append("# line ignored by Ruby:"); // prefix with a comment so it's ignored by parser
                        content.append(line);
                        content.append("\n");
                    }
                }
            }

            final char[] buffer = new char[1024];
            while (true) {
                final int read = bufferedReader.read(buffer, 0, buffer.length);
                if (read < 0) {
                    break;
                } else {
                    content.append(buffer, 0, read);
                }
            }

            return content.toString();
        }
    }

    private boolean isCurrentLineShebang(BufferedReader bufferedReader) throws IOException {
        final char[] buffer = new char[2];
        bufferedReader.mark(2);
        bufferedReader.read(buffer, 0, 2);
        bufferedReader.reset();
        return buffer[0] == '#' && buffer[1] == '!';
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

        final String mimeType;
        if (feature.toLowerCase().endsWith(RubyLanguage.CEXT_EXTENSION)) {
            mimeType = RubyLanguage.CEXT_MIME_TYPE;
        } else {
            // We need to assume all other files are Ruby, so the file type detection isn't enough
            mimeType = RubyLanguage.MIME_TYPE;
        }

        String name = feature;
        if (context != null && context.isPreInitializing()) {
            name = RUBY_HOME_SCHEME + Paths.get(context.getRubyHome()).relativize(Paths.get(feature));
        }

        return new RubySource(buildSource(
                Source.newBuilder(new File(feature)).name(name.intern()).mimeType(mimeType), internal));
    }

    private boolean isInternal(String canonicalPath) {
        if (canonicalPath.startsWith(context.getCoreLibrary().getCoreLoadPath())) {
            return context.getOptions().CORE_AS_INTERNAL;
        }

        if (canonicalPath.startsWith(context.getRubyHome() + "/lib/") &&
                !canonicalPath.startsWith(context.getRubyHome() + "/lib/ruby/gems/")) {
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
