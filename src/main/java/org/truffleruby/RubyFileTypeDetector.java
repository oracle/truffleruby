/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import com.oracle.truffle.api.TruffleFile;
import org.jcodings.Encoding;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.parser.lexer.RubyLexer;

public class RubyFileTypeDetector implements TruffleFile.FileTypeDetector {

    private static final String MIME_TYPE = "application/x-ruby";

    private static final String[] KNOWN_RUBY_FILES = new String[]{ "Gemfile", "Rakefile", "Mavenfile" };
    private static final String[] KNOWN_RUBY_SUFFIXES = new String[]{ ".rb", ".rake", ".gemspec" };
    private static final Pattern SHEBANG_REGEXP = Pattern.compile("^#! ?/usr/bin/(env +ruby|ruby).*");

    @Override
    public String findMimeType(TruffleFile file) throws IOException {
        return findMimeAndEncodingImpl(file, null);
    }

    @Override
    public Charset findEncoding(TruffleFile file) throws IOException {
        Charset[] encodingHolder = new Charset[1];
        findMimeAndEncodingImpl(file, encodingHolder);
        return encodingHolder[0];
    }

    private String findMimeAndEncodingImpl(TruffleFile file, Charset[] encodingHolder) {
        final String fileName = file.getName();

        if (fileName == null) {
            return null;
        }

        final String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
        String mimeType = null;

        for (String candidate : KNOWN_RUBY_SUFFIXES) {
            if (lowerCaseFileName.endsWith(candidate)) {
                mimeType = MIME_TYPE;
                break;
            }
        }

        if (mimeType == null) {
            for (String candidate : KNOWN_RUBY_FILES) {
                if (fileName.equals(candidate)) {
                    mimeType = MIME_TYPE;
                    break;
                }
            }
        }

        if (mimeType == null || encodingHolder != null) {
            try (BufferedReader fileContent = file.newBufferedReader(StandardCharsets.UTF_8)) {
                final String firstLine = fileContent.readLine();
                if (firstLine != null) {
                    String encodingCommentLine;
                    if (SHEBANG_REGEXP.matcher(firstLine).matches()) {
                        mimeType = mimeType == null ? MIME_TYPE : mimeType;
                        encodingCommentLine = encodingHolder == null ? null : fileContent.readLine();
                    } else {
                        encodingCommentLine = encodingHolder == null ? null : firstLine;
                    }
                    if (encodingCommentLine != null) {
                        Rope encodingCommentRope = StringOperations.encodeRope(encodingCommentLine, EncodingManager.getEncoding("UTF-8"));
                        RubyLexer.parseMagicComment(encodingCommentRope, new BiConsumer<String, Rope>() {
                            @Override
                            public void accept(String name, Rope value) {
                                if (RubyLexer.isMagicEncodingComment(name)) {
                                    Encoding encoding = EncodingManager.getEncoding(value);
                                    if (encoding != null) {
                                        encodingHolder[0] = encoding.getCharset();
                                    }
                                }
                            }
                        });
                    }
                }
            } catch (IOException | SecurityException e) {
                // Reading random files as UTF-8 could cause all sorts of errors
            }
        }

        return mimeType;
    }

}
