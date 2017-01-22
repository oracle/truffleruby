/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.loader.SourceLoader;
import org.truffleruby.options.OptionsCatalog;
import org.truffleruby.parser.ast.RootParseNode;
import org.truffleruby.parser.parser.ParserConfiguration;
import org.truffleruby.parser.scope.DynamicScope;
import org.truffleruby.parser.scope.StaticScope;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ParserCache {

    /*
     * The parser cache is deliberately per-VM (static), so that it is built into the AOT image, and so that it can be
     * reused for multiple Ruby contexts in the same VM. Also note that the parser cache may not be safe for all Ruby
     * code. Things may be wrong for code that causes side effects when parsed (such as syntax errors) for example. We
     * only use it for code that we control at the moment therefore.
     */

    public static final ParserCache INSTANCE = new ParserCache();

    static {
        /*
         * Speculatively cache the files in the default core load path - if people have set it to something else then
         * it'll be wasted work, but it's probably a developer and it doesn't matter too much.
         */

        final String defaultCoreLibraryPath = OptionsCatalog.CORE_LOAD_PATH.getDefaultValue();

        for (String coreFile : CoreLibrary.CORE_FILES) {
            INSTANCE.add(defaultCoreLibraryPath + coreFile);
        }
    }

    private final Map<String, RootParseNode> cache = new ConcurrentHashMap<>();

    public void add(String canonicalPath) {
        if (TruffleOptions.AOT) {
            cache.put(canonicalPath, load(canonicalPath));
        } else {
            ForkJoinPool.commonPool().submit(() -> cache.put(canonicalPath, load(canonicalPath)));
        }
    }

    private RootParseNode load(String canonicalPath) {
        final TranslatorDriver driver = new TranslatorDriver(null);
        final StaticScope staticScope = new StaticScope(StaticScope.Type.LOCAL, null);
        final DynamicScope dynamicScope = new DynamicScope(staticScope);
        final ParserConfiguration parserConfiguration = new ParserConfiguration(null, 0, false, true, false);
        final Source source;
        try {
            source = SourceLoader.loadResource(canonicalPath);
        } catch (IOException e) {
            throw new JavaException(e);
        }
        return driver.parse(source, dynamicScope, parserConfiguration);
    }

    public RootParseNode lookup(String canonicalPath) {
        return cache.get(canonicalPath);
    }

}
