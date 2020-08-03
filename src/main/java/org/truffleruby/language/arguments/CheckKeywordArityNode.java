/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.BiConsumerNode;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.HashNodes.EachKeyValueNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class CheckKeywordArityNode extends RubyContextSourceNode {

    private final Arity arity;
    @Child private RubyNode body;

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private CheckKeywordArgumentsNode checkKeywordArgumentsNode;
    @Child private EachKeyValueNode eachKeyNode;

    private final BranchProfile receivedKeywordsProfile = BranchProfile.create();
    private final BranchProfile basicArityCheckFailedProfile = BranchProfile.create();

    public CheckKeywordArityNode(RubyLanguage language, Arity arity, RubyNode body) {
        this.arity = arity;
        this.body = body;
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(arity.getRequired());
        this.checkKeywordArgumentsNode = new CheckKeywordArgumentsNode(language, arity);
        this.eachKeyNode = EachKeyValueNode.create();

    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        checkArity(frame);
        body.doExecuteVoid(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        checkArity(frame);
        return body.execute(frame);
    }

    private void checkArity(VirtualFrame frame) {
        final RubyHash keywordArguments = readUserKeywordsHashNode.execute(frame);

        int given = RubyArguments.getArgumentsCount(frame);

        if (keywordArguments != null) {
            receivedKeywordsProfile.enter();
            given -= 1;
        }

        if (!CheckArityNode.checkArity(arity, given)) {
            basicArityCheckFailedProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().argumentError(given, arity.getRequired(), this));
        }

        if (keywordArguments != null) {
            receivedKeywordsProfile.enter();
            eachKeyNode.executeEachKeyValue(frame, keywordArguments, checkKeywordArgumentsNode, null);
        }
    }

    private static class CheckKeywordArgumentsNode extends RubyContextNode implements BiConsumerNode {

        private final boolean checkAllowedKeywords;
        private final boolean doesNotAcceptExtraArguments;
        private final int required;
        @CompilationFinal(dimensions = 1) private final RubySymbol[] allowedKeywords;

        private final ConditionProfile isSymbolProfile = ConditionProfile.create();
        private final BranchProfile tooManyKeywordsProfile = BranchProfile.create();
        private final BranchProfile unknownKeywordProfile;

        public CheckKeywordArgumentsNode(RubyLanguage language, Arity arity) {
            checkAllowedKeywords = !arity.hasKeywordsRest();
            doesNotAcceptExtraArguments = !arity.hasRest() && arity.getOptional() == 0;
            required = arity.getRequired();
            allowedKeywords = checkAllowedKeywords ? keywordsAsSymbols(language, arity) : null;
            unknownKeywordProfile = checkAllowedKeywords ? BranchProfile.create() : null;
        }

        @Override
        public void accept(VirtualFrame frame, Object key, Object value, Object state) {
            if (isSymbolProfile.profile(key instanceof RubySymbol)) {
                if (checkAllowedKeywords && !keywordAllowed(key)) {
                    unknownKeywordProfile.enter();
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentErrorUnknownKeyword((RubySymbol) key, this));
                }
            } else {
                final int given = RubyArguments.getArgumentsCount(frame); // -1 for keyword hash, +1 for reject Hash with non-Symbol key
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
