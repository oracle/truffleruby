/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;

class CheckKeywordArgumentsNode extends RubyBaseNode implements HashStoreLibrary.EachEntryCallback {

    private final boolean doesNotAcceptExtraArguments;
    private final int required;
    @CompilerDirectives.CompilationFinal(dimensions = 1) private final RubySymbol[] allowedKeywords;

    public CheckKeywordArgumentsNode(RubyLanguage language, Arity arity) {
        assert !arity.hasKeywordsRest();
        doesNotAcceptExtraArguments = !arity.hasRest() && arity.getOptional() == 0;
        required = arity.getRequired();
        allowedKeywords = keywordsAsSymbols(language, arity);
    }

    private static RubySymbol[] keywordsAsSymbols(RubyLanguage language, Arity arity) {
        final String[] names = arity.getKeywordArguments();
        final RubySymbol[] symbols = new RubySymbol[names.length];
        for (int i = 0; i < names.length; i++) {
            symbols[i] = language.getSymbol(names[i]);
        }
        return symbols;
    }

    @Override
    public void accept(int index, Object key, Object value, Object argumentsCount) {
        if (key instanceof RubySymbol) {
            if (!keywordAllowed(key)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentErrorUnknownKeyword((RubySymbol) key, this));
            }
        } else {
            // the Hash would be split and a reject Hash be created to hold non-Symbols when there is no **kwrest parameter,
            // so we need to check if an extra argument is allowed
            final int given = (int) argumentsCount; // -1 for keyword hash, +1 for reject Hash with non-Symbol keys
            if (doesNotAcceptExtraArguments && given > required) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(given, required, this));
            }
        }

    }

    @ExplodeLoop
    private boolean keywordAllowed(Object keyword) {
        for (int i = 0; i < allowedKeywords.length; i++) {
            if (allowedKeywords[i] == keyword) {
                return true;
            }
        }

        return false;
    }
}
