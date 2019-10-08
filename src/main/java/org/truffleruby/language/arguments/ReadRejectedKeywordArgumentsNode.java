/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.collections.BiConsumerNode;
import org.truffleruby.core.hash.HashNodes.EachKeyValueNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.SetNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadRejectedKeywordArgumentsNode extends RubyBaseNode implements BiConsumerNode {

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private EachKeyValueNode eachKeyNode = EachKeyValueNode.create();
    @Child private SetNode setNode = SetNode.create();

    private final ConditionProfile isSymbolProfile = ConditionProfile.createBinaryProfile();

    public DynamicObject extractRejectedKwargs(VirtualFrame frame, DynamicObject kwargsHash) {
        final DynamicObject rejectedKwargs = HashOperations.newEmptyHash(getContext());
        eachKeyNode.executeEachKeyValue(frame, kwargsHash, this, rejectedKwargs);
        return rejectedKwargs;
    }

    @Override
    public void accept(VirtualFrame frame, Object key, Object value, Object rejectedKwargs) {
        if (!isSymbolProfile.profile(RubyGuards.isRubySymbol(key))) {
            setNode.executeSet((DynamicObject) rejectedKwargs, key, value, false);
        }
    }

}
