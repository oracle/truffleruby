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

import org.truffleruby.Log;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.RubySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
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

    public String getPath(Source source) {
        final String name = source.getName();
        if (context.wasPreInitialized() && name.startsWith(RUBY_HOME_SCHEME)) {
            return context.getRubyHome() + "/" + name.substring(RUBY_HOME_SCHEME.length());
        } else {
            return name;
        }
    }

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

        final File file = new File(path).getCanonicalFile();
        ensureReadable(context, path, file);

        mainSource = Source.newBuilder(file).name(path).content(xOptionStrip(
                currentNode,
                new FileReader(file))).mimeType(RubyLanguage.MIME_TYPE).build();
        mainSourceAbsolutePath = file.getPath();
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
    public RubySource load(String feature) throws IOException {
        if (context.getOptions().LOG_LOAD) {
            Log.LOGGER.info("loading " + feature);
        }

        return loadNoLogging(context, feature, isInternal(feature));
    }

    public static RubySource loadNoLogging(RubyContext context, String feature, boolean internal) throws IOException {
        if (feature.startsWith(RESOURCE_SCHEME)) {
            return loadResource(feature);
        } else {
            final File file = new File(feature).getCanonicalFile();
            ensureReadable(context, feature, file);

            final String mimeType;

            if (feature.toLowerCase().endsWith(RubyLanguage.CEXT_EXTENSION)) {
                mimeType = RubyLanguage.CEXT_MIME_TYPE;
            } else {
                // We need to assume all other files are Ruby, so the file type detection isn't
                // enough
                mimeType = RubyLanguage.MIME_TYPE;
            }

            String name = file.getPath();
            if (context != null && context.isPreInitializing()) {
                name = RUBY_HOME_SCHEME + Paths.get(context.getRubyHome()).relativize(Paths.get(file.getPath()));
            }

            Source.Builder<IOException, RuntimeException, RuntimeException> builder =
                    Source.newBuilder(file).name(name.intern()).mimeType(mimeType);

            if (internal) {
                builder = builder.internal();
            }

            return new RubySource(builder.build());
        }
    }

    private boolean isInternal(String canonicalPath) {
        if (canonicalPath.startsWith(context.getCoreLibrary().getCoreLoadPath())) {
            return true;
        }

        if (canonicalPath.startsWith(context.getRubyHome())) {
            return true;
        }

        return false;
    }

    private static RubySource loadResource(String path) throws IOException {
        if (TruffleOptions.AOT) {
            final String canonicalPath = SourceLoaderSupport.canonicalizeResourcePath(path);
            final SourceLoaderSupport.CoreLibraryFile coreFile = SourceLoaderSupport.allCoreLibraryFiles.get(canonicalPath);
            if (coreFile == null) {
                throw new FileNotFoundException(path);
            }

            final Source source = Source.newBuilder(coreFile.code).name(path).mimeType(RubyLanguage.MIME_TYPE).internal().build();
            return new RubySource(source);
        } else {
            if (!path.toLowerCase(Locale.ENGLISH).endsWith(".rb")) {
                throw new FileNotFoundException(path);
            }

            final Class<?> relativeClass = RubyContext.class;
            final Path relativePath = FileSystems.getDefault().getPath(path.substring(RESOURCE_SCHEME.length()));

            final Path normalizedPath = relativePath.normalize();
            final InputStream stream = relativeClass.getResourceAsStream(
                    StringUtils.replace(normalizedPath.toString(), '\\', '/'));

            if (stream == null) {
                throw new FileNotFoundException(path);
            }

            final Source source = Source.newBuilder(new InputStreamReader(stream, StandardCharsets.UTF_8)).name(path).
                    mimeType(RubyLanguage.MIME_TYPE).internal().build();
            return new RubySource(source);
        }
    }

    private static void ensureReadable(RubyContext context, String path, File file) {
        if (context != null) {
            if (!file.exists()) {
                throw new RaiseException(context, context.getCoreExceptions().loadError("No such file or directory -- " + path, path, null));
            }

            if (!file.canRead()) {
                throw new RaiseException(context, context.getCoreExceptions().loadError("Permission denied -- " + path, path, null));
            }
        }
    }

}
