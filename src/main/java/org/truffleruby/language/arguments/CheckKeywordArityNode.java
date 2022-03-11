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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;

public class CheckKeywordArityNode extends RubyBaseNode {

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private CheckKeywordArgumentsNode checkKeywordArgumentsNode;
    @Child private HashStoreLibrary hashes;

    @CompilationFinal private boolean basicArityCheckFailedProfile;

    public CheckKeywordArityNode() {
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode();
    }

    public void checkArity(VirtualFrame frame, Arity arity) {
        CompilerAsserts.partialEvaluationConstant(arity);

        final int positionalArgumentsCount = RubyArguments.getPositionalArgumentsCount(frame, true);
        if (!arity.basicCheck(positionalArgumentsCount)) {
            if (!basicArityCheckFailedProfile) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                basicArityCheckFailedProfile = true;
            }

            throw new RaiseException(getContext(),
                    coreExceptions().argumentError(positionalArgumentsCount, arity.getRequired(), this));
        }


        if (!arity.hasKeywordsRest()) {
            final RubyHash keywordArguments = readUserKeywordsHashNode.execute(frame);
            if (keywordArguments != null) {
                checkKeywordArguments(keywordArguments, arity, getLanguage());
            }
        }
    }

    void checkKeywordArguments(RubyHash keywordArguments, Arity arity, RubyLanguage language) {
        if (hashes == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashes = insert(HashStoreLibrary.createDispatched());
        }
        if (checkKeywordArgumentsNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            checkKeywordArgumentsNode = insert(new CheckKeywordArgumentsNode(language, arity));
        }
        hashes.eachEntry(keywordArguments.store, keywordArguments, checkKeywordArgumentsNode, null);
    }

    private static class CheckKeywordArgumentsNode extends RubyBaseNode implements EachEntryCallback {

        @CompilationFinal(dimensions = 1) private final RubySymbol[] allowedKeywords;

        private final BranchProfile unknownKeywordProfile = BranchProfile.create();

        public CheckKeywordArgumentsNode(RubyLanguage language, Arity arity) {
            assert !arity.hasKeywordsRest();
            allowedKeywords = keywordsAsSymbols(language, arity);
        }

        @Override
        public void accept(int index, Object key, Object value, Object state) {
            if (!keywordAllowed(key)) {
                unknownKeywordProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentErrorUnknownKeyword(key, this));
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
