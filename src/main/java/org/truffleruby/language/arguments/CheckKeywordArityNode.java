/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class CheckKeywordArityNode extends RubyBaseNode {

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private CheckKeywordArgumentsNode checkKeywordArgumentsNode;
    @Child private HashStoreLibrary hashes;

    private final BranchProfile receivedKeywordsProfile = BranchProfile.create();

    public CheckKeywordArityNode(Arity arity) {
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(arity.getRequired());
    }

    public void checkArity(VirtualFrame frame, Arity arity,
            BranchProfile basicArityCheckFailedProfile,
            RubyLanguage language,
            ContextReference<RubyContext> contextRef) {
        CompilerAsserts.partialEvaluationConstant(arity);

        final RubyHash keywordArguments = readUserKeywordsHashNode.execute(frame);

        int given = RubyArguments.getArgumentsCount(frame);

        if (keywordArguments != null) {
            receivedKeywordsProfile.enter();
            given -= 1;
        }

        if (!arity.basicCheck(given)) {
            basicArityCheckFailedProfile.enter();
            throw new RaiseException(
                    contextRef.get(),
                    contextRef.get().getCoreExceptions().argumentError(given, arity.getRequired(), this));
        }

        if (!arity.hasKeywordsRest() && keywordArguments != null) {
            checkKeywordArguments(frame, keywordArguments, arity, language);
        }
    }

    void checkKeywordArguments(VirtualFrame frame, RubyHash keywordArguments, Arity arity, RubyLanguage language) {
        if (hashes == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashes = insert(HashStoreLibrary.getDispatched());
        }
        if (checkKeywordArgumentsNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            checkKeywordArgumentsNode = insert(new CheckKeywordArgumentsNode(language, arity));
        }
        hashes.eachEntry(keywordArguments.store, frame, keywordArguments, checkKeywordArgumentsNode, null);
    }

    private static class CheckKeywordArgumentsNode extends RubyContextNode implements EachEntryCallback {

        private final boolean doesNotAcceptExtraArguments;
        private final int required;
        @CompilationFinal(dimensions = 1) private final RubySymbol[] allowedKeywords;

        private final ConditionProfile isSymbolProfile = ConditionProfile.create();
        private final BranchProfile tooManyKeywordsProfile = BranchProfile.create();
        private final BranchProfile unknownKeywordProfile = BranchProfile.create();

        public CheckKeywordArgumentsNode(RubyLanguage language, Arity arity) {
            assert !arity.hasKeywordsRest();
            doesNotAcceptExtraArguments = !arity.hasRest() && arity.getOptional() == 0;
            required = arity.getRequired();
            allowedKeywords = keywordsAsSymbols(language, arity);
        }

        @Override
        public void accept(VirtualFrame frame, int index, Object key, Object value, Object state) {
            if (isSymbolProfile.profile(key instanceof RubySymbol)) {
                if (!keywordAllowed(key)) {
                    unknownKeywordProfile.enter();
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentErrorUnknownKeyword((RubySymbol) key, this));
                }
            } else {
                // the Hash would be split and a reject Hash be created to hold non-Symbols when there is no **kwrest parameter,
                // so we need to check if an extra argument is allowed
                final int given = RubyArguments.getArgumentsCount(frame); // -1 for keyword hash, +1 for reject Hash with non-Symbol keys
                if (doesNotAcceptExtraArguments && given > required) {
                    tooManyKeywordsProfile.enter();
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

    static RubySymbol[] keywordsAsSymbols(RubyLanguage language, Arity arity) {
        final String[] names = arity.getKeywordArguments();
        final RubySymbol[] symbols = new RubySymbol[names.length];
        for (int i = 0; i < names.length; i++) {
            symbols[i] = language.getSymbol(names[i]);
        }
        return symbols;
    }
}
