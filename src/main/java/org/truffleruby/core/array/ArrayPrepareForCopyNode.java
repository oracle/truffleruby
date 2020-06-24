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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextNode;

import java.util.Arrays;

import static org.truffleruby.Layouts.ARRAY;

/** This node prepares an array to receive content copied from another array. In particular:
 * <ul>
 * <li>It makes sure that the array's store's capacity is sufficient to handle the copy, and update its size.
 * <li>It makes sure the array's store is mutable, and compatible with the source array, generalizing it if needed.
 * <li>If the copy is done entirely beyond the current size of the array, it makes sure intervening slots are filled
 * with {@code nil}.
 * </ul>
 * <p>
 * The copy itself can then be performed with {@link ArrayCopyCompatibleRangeNode}. In fact it MUST be performed, as the
 * array may otherwise contain uninitialized slots (in particular, {@code null} values in {@code Object} arrays). */
@ReportPolymorphism
@ImportStatic(ArrayGuards.class)
public abstract class ArrayPrepareForCopyNode extends RubyContextNode {

    public static ArrayPrepareForCopyNode create() {
        return ArrayPrepareForCopyNodeGen.create();
    }

    public abstract void execute(DynamicObject dst, DynamicObject src, int dstStart, int length);

    @ReportPolymorphism.Exclude
    @Specialization(guards = { "length == 0", "start <= getSize(dst)" })
    protected void noChange(DynamicObject dst, DynamicObject src, int start, int length) {
    }

    @Specialization(
            guards = "start > getSize(dst)",
            limit = "storageStrategyLimit()")
    protected void nilPad(DynamicObject dst, DynamicObject src, int start, int length,
            @CachedLibrary("getStore(dst)") ArrayStoreLibrary dstStores) {

        final int oldSize = ARRAY.getSize(dst);
        final Object oldStore = ARRAY.getStore(dst);
        final Object[] newStore = new Object[ArrayUtils.capacity(getContext(), oldSize, start + length)];
        dstStores.copyContents(oldStore, 0, newStore, 0, oldSize); // copy the original store
        Arrays.fill(newStore, oldSize, start, nil); // nil-pad the new empty part
        ARRAY.setStore(dst, newStore);
        ARRAY.setSize(dst, start + length);
    }

    @Specialization(
            guards = { "length > 0", "start <= getSize(dst)", "compatible(dstStores, dst, src)" },
            limit = "storageStrategyLimit()")
    protected void resizeCompatible(DynamicObject dst, DynamicObject src, int start, int length,
            @Cached ArrayEnsureCapacityNode ensureCapacityNode,
            @CachedLibrary("getStore(dst)") ArrayStoreLibrary dstStores) {

        // Necessary even if under capacity to ensure that the destination gets a mutable store.
        ensureCapacityNode.executeEnsureCapacity(dst, start + length);
        if (start + length > ARRAY.getSize(dst)) {
            ARRAY.setSize(dst, start + length);
        }
    }

    @Specialization(
            guards = { "length > 0", "start <= getSize(dst)", "!compatible(dstStores, dst, src)" },
            limit = "storageStrategyLimit()")
    protected void resizeGeneralize(DynamicObject dst, DynamicObject src, int start, int length,
            @CachedLibrary("getStore(dst)") ArrayStoreLibrary dstStores) {

        final int oldDstSize = ARRAY.getSize(dst);
        final int newDstSize = Math.max(oldDstSize, start + length);
        final Object dstStore = ARRAY.getStore(dst);
        final Object srcStore = ARRAY.getStore(src);
        final Object newDstStore = dstStores.allocateForNewStore(dstStore, srcStore, newDstSize);

        // NOTE(norswap, 10 Jun 2020)
        //  The semantics of this node guarantee that the whole array will be copied (some code relies on it).
        //  Otherwise, we could copy only up to `start` if we could assume the rest of the array will be filled, and copy
        //  the rest ([start + length, oldDstSize[) only if actually required.
        //  It's not clear that even in that case much would be gained by splitting the copy in two parts, unless
        //  `length` is truly big.
        dstStores.copyContents(dstStore, 0, newDstStore, 0, oldDstSize);
        ARRAY.setStore(dst, newDstStore);
        if (newDstSize > oldDstSize) {
            ARRAY.setSize(dst, newDstSize);
        }
    }

    protected static boolean compatible(ArrayStoreLibrary stores, DynamicObject dst, DynamicObject src) {
        return stores.acceptsAllValues(ARRAY.getStore(dst), ARRAY.getStore(src));
    }
}
