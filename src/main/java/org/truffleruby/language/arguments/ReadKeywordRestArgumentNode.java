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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.hash.BucketsStrategy;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.KeyValue;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

import java.util.ArrayList;
import java.util.List;

public class ReadKeywordRestArgumentNode extends RubyNode {

    private final String[] excludedKeywords;

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private NotOptimizedWarningNode notOptimizedWarningNode = new NotOptimizedWarningNode();

    private final ConditionProfile noHash = ConditionProfile.createBinaryProfile();

    public ReadKeywordRestArgumentNode(int minimum, String[] excludedKeywords) {
        this.excludedKeywords = excludedKeywords;
        readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimum);
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
    private Object extractKeywordHash(final Object hash) {
        final DynamicObject hashObject = (DynamicObject) hash;

        final List<KeyValue> entries = new ArrayList<>();

        outer: for (KeyValue keyValue : HashOperations.iterableKeyValues(hashObject)) {
            if (!RubyGuards.isRubySymbol(keyValue.getKey())) {
                continue;
            }

            for (String excludedKeyword : excludedKeywords) {
                if (excludedKeyword.equals(keyValue.getKey().toString())) {
                    continue outer;
                }
            }

            entries.add(keyValue);
        }

        return BucketsStrategy.create(getContext(), entries, Layouts.HASH.getCompareByIdentity(hashObject));
    }

}
