/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.aot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.loader.ResourceLoader;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorDriver;
import org.truffleruby.parser.ast.RootParseNode;
import org.truffleruby.parser.parser.ParserConfiguration;
import org.truffleruby.parser.scope.StaticScope;
import org.truffleruby.shared.options.OptionsCatalog;

import com.oracle.truffle.api.TruffleOptions;

public class ParserCache {

    public static final Map<String, RootParseNode> INSTANCE;

    static {
        if (TruffleOptions.AOT) {
            final String defaultCoreLibraryPath = OptionsCatalog.CORE_LOAD_PATH_KEY.getDefaultValue();
            final Map<String, RootParseNode> cache = new HashMap<>();

            for (String coreFile : CoreLibrary.CORE_FILES) {
                //intern() to improve footprint
                final String path = (defaultCoreLibraryPath + coreFile).intern();
                final RubySource source = loadSource(path);
                cache.put(path, parse(source));
            }

            INSTANCE = cache;
        } else {
            INSTANCE = null;
        }
    }

    private static RubySource loadSource(String feature) {
        try {
            final ResourceLoader resourceLoader = new ResourceLoader();
            return resourceLoader.loadResource(feature, true);
        } catch (IOException e) {
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

    private static RootParseNode parse(RubySource source) {
        final StaticScope staticScope = new StaticScope(StaticScope.Type.LOCAL, null);
        final ParserConfiguration parserConfiguration = new ParserConfiguration(null, false, true, false);

        return TranslatorDriver.parseToJRubyAST(null, source, staticScope, parserConfiguration);
    }

}
