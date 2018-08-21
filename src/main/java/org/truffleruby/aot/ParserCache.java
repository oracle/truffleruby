/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.language.loader.ResourceLoader;
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

    public static final Map<String, RootParseNode> INSTANCE;

    static {
        if (TruffleOptions.AOT) {
            final String defaultCoreLibraryPath = OptionsCatalog.CORE_LOAD_PATH.getDefaultValue();
            final Map<String, RootParseNode> cache = new HashMap<>();

            for (String coreFile : CoreLibrary.CORE_FILES) {
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
            throw new JavaException(e);
        }
    }

    private static RootParseNode parse(RubySource source) {
        final TranslatorDriver driver = new TranslatorDriver(null);
        final StaticScope staticScope = new StaticScope(StaticScope.Type.LOCAL, null);
        final ParserConfiguration parserConfiguration = new ParserConfiguration(null, false, true, false);

        return driver.parseToJRubyAST(source, staticScope, parserConfiguration);
    }

}
