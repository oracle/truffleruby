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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.HashNodes.HashLookupOrExecuteDefaultNode;
import org.truffleruby.language.RubyNode;

public class ReadKeywordArgumentNode extends RubyNode implements BiFunctionNode {

    private final DynamicObject name;
    private final ConditionProfile defaultProfile = ConditionProfile.createBinaryProfile();
    
    @Child private RubyNode defaultValue;
    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private HashLookupOrExecuteDefaultNode hashLookupNode;

    public ReadKeywordArgumentNode(int minimum, String name, RubyNode defaultValue) {
        this.name = getSymbol(name);
        this.defaultValue = defaultValue;
        readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimum);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final DynamicObject hash = readUserKeywordsHashNode.execute(frame);

        if (defaultProfile.profile(hash == null)) {
            return defaultValue.execute(frame);
        }

        return lookupKeywordInHash(frame, hash);
    }

    private Object lookupKeywordInHash(VirtualFrame frame, DynamicObject hash) {
        if (hashLookupNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashLookupNode = insert(HashLookupOrExecuteDefaultNode.create());
        }

        return hashLookupNode.executeGet(frame, hash, name, this);
    }

    @Override
    public Object accept(VirtualFrame frame, Object hash, Object key) {
        return defaultValue.execute(frame);
    }

}
