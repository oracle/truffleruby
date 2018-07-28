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
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.hash.BucketsStrategy;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.KeyValue;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.methods.Arity;

import java.util.ArrayList;
import java.util.List;

public class ReadKeywordRestArgumentNode extends RubyNode {

    @CompilationFinal(dimensions = 1) private final DynamicObject[] excludedKeywords;

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private NotOptimizedWarningNode notOptimizedWarningNode = new NotOptimizedWarningNode();

    private final ConditionProfile noHash = ConditionProfile.createBinaryProfile();

    public ReadKeywordRestArgumentNode(int minimum, Arity arity) {
        this.excludedKeywords = keywordsAsSymbols(arity);
        this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimum);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return lookupRestKeywordArgumentHash(frame);
    }

    private Object lookupRestKeywordArgumentHash(VirtualFrame frame) {
        final Object hash = readUserKeywordsHashNode.execute(frame);

        if (noHash.profile(hash == null)) {
            return coreLibrary().getHashFactory().newInstance(Layouts.HASH.build(null, 0, null, null, nil(), nil(), false));
        }

        notOptimizedWarningNode.warn("keyword arguments are not yet optimized");

        return extractKeywordHash(hash);
    }

    @TruffleBoundary
    private Object extractKeywordHash(Object hash) {
        final DynamicObject hashObject = (DynamicObject) hash;

        final List<KeyValue> entries = new ArrayList<>();

        for (KeyValue keyValue : HashOperations.iterableKeyValues(hashObject)) {
            final Object key = keyValue.getKey();

            if (RubyGuards.isRubySymbol(key) && !keywordExcluded(key)) {
                entries.add(keyValue);
            }
        }

        return BucketsStrategy.create(getContext(), entries, Layouts.HASH.getCompareByIdentity(hashObject));

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
