/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Check that no extra keyword arguments are given, when there is no **kwrest */
public class CheckKeywordArityNode extends RubyBaseNode {

    public final Arity arity;
    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private CheckExtraKeywordArgumentsNode checkExtraKeywordArgumentsNode;

    public CheckKeywordArityNode(Arity arity) {
        assert !arity.hasKeywordsRest() : "no need to create this node";
        this.arity = arity;
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode();
    }

    public void checkArity(VirtualFrame frame) {
        final RubyHash keywordArguments = readUserKeywordsHashNode.execute(frame);
        if (keywordArguments != null) {
            checkKeywordArguments(keywordArguments);
        }
    }

    private void checkKeywordArguments(RubyHash keywordArguments) {
        if (checkExtraKeywordArgumentsNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            checkExtraKeywordArgumentsNode = insert(new CheckExtraKeywordArgumentsNode(getLanguage(), arity));
        }

        checkExtraKeywordArgumentsNode.check(keywordArguments);
    }

    private static class CheckExtraKeywordArgumentsNode extends RubyBaseNode implements EachEntryCallback {

        @CompilationFinal(dimensions = 1) private final RubySymbol[] allowedKeywords;

        private final BranchProfile unknownKeywordProfile = BranchProfile.create();
        @Child private HashStoreLibrary hashes;

        public CheckExtraKeywordArgumentsNode(RubyLanguage language, Arity arity) {
            assert !arity.hasKeywordsRest();
            hashes = HashStoreLibrary.createDispatched();
            allowedKeywords = keywordsAsSymbols(language, arity);
        }

        public void check(RubyHash keywordArguments) {
            hashes.eachEntry(keywordArguments.store, keywordArguments, this, keywordArguments);
        }

        @Override
        public void accept(int index, Object key, Object value, Object state) {
            if (!keywordAllowed(key)) {
                unknownKeywordProfile.enter();
                RubyHash keywordArguments = (RubyHash) state;
                throw new RaiseException(getContext(), unknownKeywordsError(keywordArguments));
            }
        }

        @ExplodeLoop
        private boolean keywordAllowed(Object keyword) {
            for (RubySymbol allowedKeyword : allowedKeywords) {
                if (allowedKeyword == keyword) {
                    return true;
                }
            }

            return false;
        }

        @TruffleBoundary
        private RubyException unknownKeywordsError(RubyHash keywordArguments) {
            Object[] keys = findExtraKeywordArguments(keywordArguments);
            return coreExceptions().argumentErrorUnknownKeywords(keys, this);
        }

        @TruffleBoundary
        @SuppressWarnings("unchecked")
        private Object[] findExtraKeywordArguments(RubyHash keywordArguments) {
            final ArrayList<Object> actualKeywordsAsList = new ArrayList<>();
            hashes.eachEntry(
                    keywordArguments.store,
                    keywordArguments,
                    (index, key, value, state) -> ((ArrayList<Object>) state).add(key),
                    actualKeywordsAsList);

            final List<RubySymbol> allowedKeywordsAsList = Arrays.asList(allowedKeywords);
            final List<Object> extraKeywords = actualKeywordsAsList.stream()
                    .filter(k -> !allowedKeywordsAsList.contains(k))
                    .collect(Collectors.toList());

            return extraKeywords.toArray();
        }
    }

    static RubySymbol[] keywordsAsSymbols(RubyLanguage language, Arity arity) {
        final String[] names = arity.getKeywordArguments();
        final RubySymbol[] symbols = new RubySymbol[names.length];
        for (int i = 0; i < names.length; i++) {
            symbols[i] = language.getSymbol(names[i]);
        }
        return symbols;
    }
}
