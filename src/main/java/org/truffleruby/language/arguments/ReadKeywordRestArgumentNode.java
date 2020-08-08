/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.hash.HashNodes.EachKeyValueNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.SetNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.methods.Arity;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadKeywordRestArgumentNode extends RubyContextSourceNode implements BiConsumerNode {

    @CompilationFinal(dimensions = 1) private final RubySymbol[] excludedKeywords;

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private EachKeyValueNode eachKeyNode = EachKeyValueNode.create();
    @Child private SetNode setNode = SetNode.create();

    private final ConditionProfile noHash = ConditionProfile.create();
    private final ConditionProfile isSymbolProfile = ConditionProfile.create();

    public ReadKeywordRestArgumentNode(RubyLanguage language, int minimum, Arity arity) {
        this.excludedKeywords = CheckKeywordArityNode.keywordsAsSymbols(language, arity);
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimum);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return lookupRestKeywordArgumentHash(frame);
    }

    private Object lookupRestKeywordArgumentHash(VirtualFrame frame) {
        final RubyHash hash = readUserKeywordsHashNode.execute(frame);

        if (noHash.profile(hash == null)) {
            return HashOperations.newEmptyHash(getContext());
        } else {
            final RubyHash kwRest = HashOperations.newEmptyHash(getContext());
            return eachKeyNode.executeEachKeyValue(frame, hash, this, kwRest);
        }
    }

    @Override
    public void accept(VirtualFrame frame, Object key, Object value, Object kwRest) {
        if (isSymbolProfile.profile(key instanceof RubySymbol) && !keywordExcluded(key)) {
            setNode.executeSet((RubyHash) kwRest, key, value, false);
        }
    }

    @ExplodeLoop
    private boolean keywordExcluded(Object keyword) {
        for (int i = 0; i < excludedKeywords.length; i++) {
            if (excludedKeywords[i] == keyword) {
                return true;
            }
        }

        return false;
    }

}
