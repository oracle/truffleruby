/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.parser.lexer.RubyLexer;

import com.oracle.truffle.api.TruffleFile;

public class RubyFileTypeDetector implements TruffleFile.FileTypeDetector {

    private static final String[] KNOWN_RUBY_FILES = new String[]{ "Gemfile", "Rakefile" };
    private static final String[] KNOWN_RUBY_SUFFIXES = new String[]{ ".rb", ".rake", ".gemspec" };
    private static final Pattern SHEBANG_REGEXP = Pattern.compile("^#! ?/usr/bin/(env +ruby|ruby).*");

    @Override
    public String findMimeType(TruffleFile file) throws IOException {
        final String fileName = file.getName();

        if (fileName == null) {
            return null;
        }

        final String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);

        for (String candidate : KNOWN_RUBY_SUFFIXES) {
            if (lowerCaseFileName.endsWith(candidate)) {
                return RubyLanguage.getMimeType(false);
            }
        }

        for (String candidate : KNOWN_RUBY_FILES) {
            if (fileName.equals(candidate)) {
                return RubyLanguage.getMimeType(false);
            }
        }

        try (BufferedReader fileContent = file.newBufferedReader(StandardCharsets.UTF_8)) {
            final String firstLine = fileContent.readLine();
            if (firstLine != null && SHEBANG_REGEXP.matcher(firstLine).matches()) {
                return RubyLanguage.getMimeType(false);
            }
        } catch (IOException | SecurityException e) {
            // Reading random files as UTF-8 could cause all sorts of errors
        }
        return null;
    }

    @Override
    public Charset findEncoding(TruffleFile file) throws IOException {
        try (BufferedReader fileContent = file.newBufferedReader(StandardCharsets.UTF_8)) {
            final String firstLine = fileContent.readLine();
            if (firstLine != null) {
                String encodingCommentLine;
                if (SHEBANG_REGEXP.matcher(firstLine).matches()) {
                    encodingCommentLine = fileContent.readLine();
                } else {
                    encodingCommentLine = firstLine;
                }
                if (encodingCommentLine != null) {
                    Rope encodingCommentRope = StringOperations.encodeRope(encodingCommentLine, UTF8Encoding.INSTANCE);
                    Charset[] encodingHolder = new Charset[1];
                    RubyLexer.parseMagicComment(encodingCommentRope, (name, value) -> {
                        if (RubyLexer.isMagicEncodingComment(name)) {
                            Encoding encoding = EncodingManager.getEncoding(value);
                            if (encoding != null) {
                                encodingHolder[0] = encoding.getCharset();
                            }
                        }
                    });
                    return encodingHolder[0];
                }
            }
        } catch (IOException | SecurityException e) {
            // Reading random files as UTF-8 could cause all sorts of errors
        }
        return null;
    }
}
