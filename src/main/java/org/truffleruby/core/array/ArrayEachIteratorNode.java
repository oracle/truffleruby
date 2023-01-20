/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.profiles.IntValueProfile;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInterface;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;

@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayEachIteratorNode extends RubyBaseNode {

    public interface ArrayElementConsumerNode extends NodeInterface {
        void accept(RubyArray array, RubyProc block, Object element, int index);
    }

    @Child private ArrayEachIteratorNode recurseNode;

    @NeverDefault
    public static ArrayEachIteratorNode create() {
        return ArrayEachIteratorNodeGen.create();
    }

    public abstract RubyArray execute(RubyArray array, RubyProc block, int startAt,
            ArrayElementConsumerNode consumerNode);

    @Specialization(limit = "storageStrategyLimit()")
    protected RubyArray iterateMany(RubyArray array, RubyProc block, int startAt, ArrayElementConsumerNode consumerNode,
            // Checkstyle: stop -- Verified @Bind is not necessary here due to using `Library#accepts()`.
            @CachedLibrary("array.getStore()") ArrayStoreLibrary stores,
            // Checkstyle: resume
            @Cached LoopConditionProfile loopProfile,
            @Cached IntValueProfile arraySizeProfile,
            @Cached ConditionProfile strategyMatchProfile) {
        int i = startAt;
        try {
            for (; loopProfile.inject(i < arraySizeProfile.profile(array.size)); i++) {
                Object store = array.getStore();
                if (strategyMatchProfile.profile(stores.accepts(store))) {
                    consumerNode.accept(array, block, stores.read(store, i), i);
                } else {
                    return getRecurseNode().execute(array, block, i, consumerNode);
                }
                TruffleSafepoint.poll(this);
            }
        } finally {
            profileAndReportLoopCount(loopProfile, i - startAt);
        }

        return array;
    }

    private ArrayEachIteratorNode getRecurseNode() {
        if (recurseNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            recurseNode = insert(ArrayEachIteratorNode.create());
        }
        return recurseNode;
    }
}
