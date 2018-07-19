/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.aot;

import com.oracle.truffle.api.TruffleOptions;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.loader.SourceLoader;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorDriver;
import org.truffleruby.parser.ast.RootParseNode;
import org.truffleruby.parser.parser.ParserConfiguration;
import org.truffleruby.parser.scope.StaticScope;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParserCache {

    public static final ParserCache INSTANCE;

    static {
        if (TruffleOptions.AOT) {
            INSTANCE = new ParserCache();

            final String defaultCoreLibraryPath = OptionsCatalog.CORE_LOAD_PATH.getDefaultValue();

            for (String coreFile : CoreLibrary.CORE_FILES) {
                INSTANCE.add((defaultCoreLibraryPath + coreFile).intern());
            }
        } else {
            INSTANCE = null;
        }
    }

    private final Map<String, RootParseNode> cache = new HashMap<>();

    public void add(String feature) {
        cache.put(feature, load(feature));
    }

    private RootParseNode load(String feature) {
        final TranslatorDriver driver = new TranslatorDriver(null);
        final StaticScope staticScope = new StaticScope(StaticScope.Type.LOCAL, null);
        final ParserConfiguration parserConfiguration = new ParserConfiguration(null, 0, false, true, false);

        final RubySource source;
        try {
            source = SourceLoader.loadNoLogging(null, feature, true);
        } catch (IOException e) {
            throw new JavaException(e);
        }

        return driver.parseToJRubyAST(source, staticScope, parserConfiguration);
    }

    public RootParseNode lookup(String canonicalPath) {
        return cache.get(canonicalPath);
    }

}
