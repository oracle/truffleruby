/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;


/** Copies a portion of an array to another array, whose store is known to have sufficient capacity, and to be
 * compatible with the source array's store.
 * <p>
 * This never checks the array's size, which may therefore be adjusted afterwards.
 * <p>
 * Also propagates sharing from the source array to destination array.
 * <p>
 * Typically only called after {@link ArrayPrepareForCopyNode} has been invoked on the destination. */
@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayCopyCompatibleRangeNode extends RubyBaseNode {

    public static ArrayCopyCompatibleRangeNode create() {
        return ArrayCopyCompatibleRangeNodeGen.create();
    }

    public abstract void execute(Object dstStore, Object srcStore, int dstStart, int srcStart, int length,
            boolean dstShared, boolean srcShared);

    protected boolean noopGuard(Object dstStore, Object srcStore, int dstStart, int srcStart, int length) {
        return length == 0 || dstStore == srcStore && dstStart == srcStart;
    }

    @Specialization(guards = "noopGuard(dstStore, srcStore, dstStart, srcStart, length)")
    protected void noop(
            Object dstStore,
            Object srcStore,
            int dstStart,
            int srcStart,
            int length,
            boolean dstShared,
            boolean srcShared) {
    }

    @Specialization(
            guards = "!noopGuard(dstStore, srcStore, dstStart, srcStart, length)",
            limit = "storageStrategyLimit()")
    protected void copy(
            Object dstStore,
            Object srcStore,
            int dstStart,
            int srcStart,
            int length,
            boolean dstShared,
            boolean srcShared,
            @CachedLibrary("srcStore") ArrayStoreLibrary stores,
            @Cached WriteBarrierNode writeBarrierNode,
            @Cached ConditionProfile share,
            @Cached LoopConditionProfile loopProfile) {

        stores.copyContents(srcStore, srcStart, dstStore, dstStart, length);

        if (share.profile(!stores.isPrimitive(srcStore) &&
                dstShared &&
                !srcShared)) {
            int i = 0;
            try {
                for (; loopProfile.inject(i < length); ++i) {
                    writeBarrierNode.executeWriteBarrier(stores.read(srcStore, srcStart + i));
                }
                TruffleSafepoint.poll(this);
            } finally {
                profileAndReportLoopCount(loopProfile, i);
            }
        }
    }
}
