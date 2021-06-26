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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.source.Source;

/*
 * Loads source files that have been stored as resources (in the Java jar file sense.)
 */
public abstract class ResourceLoader {

    public static Source loadResource(String path, boolean internal) throws IOException {
        assert path.startsWith(RubyLanguage.RESOURCE_SCHEME);

        if (!path.toLowerCase(Locale.ENGLISH).endsWith(".rb")) {
            throw new FileNotFoundException(path);
        }

        final Class<?> relativeClass = RubyContext.class;
        final Path relativePath = Paths.get(path.substring(RubyLanguage.RESOURCE_SCHEME.length()));
        final String normalizedPath = StringUtils.replace(relativePath.normalize().toString(), '\\', '/');
        final InputStream stream = relativeClass.getResourceAsStream(normalizedPath);

        if (stream == null) {
            throw new FileNotFoundException(path);
        }

        final Source source;

        // We guarantee that we only put UTF-8 source files into resources
        try (final InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, reader, path).internal(internal).build();
        }

        return source;
    }

}
