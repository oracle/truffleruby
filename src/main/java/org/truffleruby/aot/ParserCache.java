/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.aot;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.source.Source;
import org.graalvm.collections.Pair;
import org.prism.ParseResult;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.loader.ResourceLoader;
import org.truffleruby.parser.ParseEnvironment;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubyDeferredWarnings;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.YARPTranslatorDriver;
import org.truffleruby.parser.parser.ParserConfiguration;
import org.truffleruby.shared.options.OptionsCatalog;

import com.oracle.truffle.api.TruffleOptions;

public final class ParserCache {

    public static final Map<String, Pair<ParseResult, Source>> INSTANCE;

    static {
        if (TruffleOptions.AOT) {
            final String defaultCoreLibraryPath = OptionsCatalog.CORE_LOAD_PATH_KEY.getDefaultValue();
            final Map<String, Pair<ParseResult, Source>> cache = new HashMap<>();

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
            final Source source = ResourceLoader
                    .loadResource(feature, OptionsCatalog.CORE_AS_INTERNAL_KEY.getDefaultValue());
            return new RubySource(source, feature);
        } catch (IOException e) {
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

    private static Pair<ParseResult, Source> parse(RubySource source) {
        var language = RubyLanguage.getCurrentLanguage();
        var parserConfiguration = new ParserConfiguration(null, false, true, false);
        var rubyWarnings = new RubyDeferredWarnings();
        var parseEnvironment = new ParseEnvironment(language, source,
                YARPTranslatorDriver.createYARPSource(source.getBytes()), ParserContext.TOP_LEVEL, null);

        var parseResult = YARPTranslatorDriver.parseToYARPAST(null, language, source, Collections.emptyList(),
                parserConfiguration,
                rubyWarnings, parseEnvironment);

        return Pair.create(parseResult, source.getSource());
    }

}
