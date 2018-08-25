/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.collections.BiConsumerNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.SetNode;
import org.truffleruby.core.hash.HashNodes.EachKeyValueNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.methods.Arity;

public class ReadKeywordRestArgumentNode extends RubyNode implements BiConsumerNode {

    @CompilationFinal(dimensions = 1) private final DynamicObject[] excludedKeywords;

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private EachKeyValueNode eachKeyNode = EachKeyValueNode.create();
    @Child private SetNode setNode = SetNode.create();

    private final ConditionProfile noHash = ConditionProfile.createBinaryProfile();
    private final ConditionProfile isSymbolProfile = ConditionProfile.createBinaryProfile();

    public ReadKeywordRestArgumentNode(int minimum, Arity arity) {
        this.excludedKeywords = keywordsAsSymbols(arity);
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimum);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return lookupRestKeywordArgumentHash(frame);
    }

    private Object lookupRestKeywordArgumentHash(VirtualFrame frame) {
        final DynamicObject hash = readUserKeywordsHashNode.execute(frame);

        if (noHash.profile(hash == null)) {
            return HashOperations.newEmptyHash(getContext());
        } else {
            final DynamicObject kwRest = HashOperations.newEmptyHash(getContext());
            return eachKeyNode.executeEachKeyValue(frame, hash, this, kwRest);
        }
    }

    @Override
    public void accept(VirtualFrame frame, Object key, Object value, Object kwRest) {
        if (isSymbolProfile.profile(RubyGuards.isRubySymbol(key)) && !keywordExcluded(key)) {
            setNode.executeSet((DynamicObject) kwRest, key, value, false);
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

    private DynamicObject[] keywordsAsSymbols(Arity arity) {
        final String[] names = arity.getKeywordArguments();
        final DynamicObject[] symbols = new DynamicObject[names.length];
        for (int i = 0; i < names.length; i++) {
            symbols[i] = getSymbol(names[i]);
        }
        return symbols;
    }

}
