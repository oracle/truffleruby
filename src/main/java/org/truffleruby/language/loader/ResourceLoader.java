/*
 * Copyright (c) 2013, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.parser.MagicCommentParser;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.source.Source;

/*
 * Loads source files that have been stored as resources (in the Java jar file sense.)
 */
public abstract class ResourceLoader {

    public static RubySource loadResource(String path, boolean internal) throws IOException {
        assert path.startsWith(RubyLanguage.RESOURCE_SCHEME);

        if (!path.toLowerCase(Locale.ENGLISH).endsWith(".rb")) {
            throw new FileNotFoundException(path);
        }

        final String resourcePath = path.substring(RubyLanguage.RESOURCE_SCHEME.length());

        final byte[] sourceBytes;
        try (InputStream stream = ResourceLoader.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new FileNotFoundException(resourcePath);
            }

            sourceBytes = stream.readAllBytes();
        }


        var sourceTString = MagicCommentParser.createSourceTStringBasedOnMagicEncodingComment(sourceBytes,
                Encodings.UTF_8);

        Source source = Source
                .newBuilder(TruffleRuby.LANGUAGE_ID, new ByteBasedCharSequence(sourceTString), path)
                .option("ruby.Coverage", "false")
                .internal(internal)
                .build();

        return new RubySource(source, path, sourceTString);
    }

}
