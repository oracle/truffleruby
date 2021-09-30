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

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;

import java.util.Arrays;

/** This node prepares an array to receive content copied from another array. In particular:
 * <ul>
 * <li>It makes sure that the array's store's capacity is sufficient to handle the copy, and update its size.
 * <li>It makes sure the array's store is mutable, and compatible with the source array, generalizing it if needed.
 * <li>If the copy is done entirely beyond the current size of the array, it makes sure intervening slots are filled
 * with {@code nil}.
 * </ul>
 * <p>
 * The copy itself can then be performed with {@link ArrayCopyCompatibleRangeNode}. In fact it MUST be performed, as the
 * array may otherwise contain uninitialized elements (in particular, {@code null} values in {@code Object} arrays). */
@ReportPolymorphism
@ImportStatic(ArrayGuards.class)
public abstract class ArrayPrepareForCopyNode extends RubyBaseNode {

    public static ArrayPrepareForCopyNode create() {
        return ArrayPrepareForCopyNodeGen.create();
    }

    public abstract void execute(RubyArray dst, RubyArray src, int dstStart, int length);

    @ReportPolymorphism.Exclude
    @Specialization(guards = { "length == 0", "start <= dst.size" })
    protected void noChange(RubyArray dst, RubyArray src, int start, int length) {
    }

    @Specialization(
            guards = "start > dst.size",
            limit = "storageStrategyLimit()")
    protected void nilPad(RubyArray dst, RubyArray src, int start, int length,
            @Bind("dst.store") Object oldStore,
            @CachedLibrary("oldStore") ArrayStoreLibrary dstStores) {

        final int oldSize = dst.size;
        final Object[] newStore = new Object[ArrayUtils.capacity(getLanguage(), oldSize, start + length)];
        dstStores.copyContents(oldStore, 0, newStore, 0, oldSize); // copy the original store
        Arrays.fill(newStore, oldSize, start, nil); // nil-pad the new empty part
        dst.store = newStore;
        dst.size = start + length;
    }

    @Specialization(
            guards = { "length > 0", "start <= dst.size", "compatible(dstStores, dst, src)" },
            limit = "storageStrategyLimit()")
    protected void resizeCompatible(RubyArray dst, RubyArray src, int start, int length,
            @Bind("dst.store") Object oldStore,
            @Cached ArrayEnsureCapacityNode ensureCapacityNode,
            @CachedLibrary("oldStore") ArrayStoreLibrary dstStores) {

        // Necessary even if under capacity to ensure that the destination gets a mutable store.
        ensureCapacityNode.executeEnsureCapacity(dst, start + length);
        if (start + length > dst.size) {
            dst.size = start + length;
        }
    }

    @Specialization(
            guards = { "length > 0", "start <= dst.size", "!compatible(dstStores, dst, src)" },
            limit = "storageStrategyLimit()")
    protected void resizeGeneralize(RubyArray dst, RubyArray src, int start, int length,
            @Bind("dst.store") Object dstStore,
            @CachedLibrary("dstStore") ArrayStoreLibrary dstStores) {

        final int oldDstSize = dst.size;
        final int newDstSize = Math.max(oldDstSize, start + length);
        final Object srcStore = src.store;
        final Object newDstStore = dstStores.allocateForNewStore(dstStore, srcStore, newDstSize);

        // NOTE(norswap, 10 Jun 2020)
        //  The semantics of this node guarantee that the whole array will be copied (some code relies on it).
        //  Otherwise, we could copy only up to `start` if we could assume the rest of the array will be filled, and copy
        //  the rest ([start + length, oldDstSize[) only if actually required.
        //  It's not clear that even in that case much would be gained by splitting the copy in two parts, unless
        //  `length` is truly big.
        dstStores.copyContents(dstStore, 0, newDstStore, 0, oldDstSize);
        dst.store = newDstStore;
        if (newDstSize > oldDstSize) {
            dst.size = newDstSize;
        }
    }

    // ToDo, we should refactor this to take stores, and bind those.
    protected static boolean compatible(ArrayStoreLibrary stores, RubyArray dst, RubyArray src) {
        return stores.acceptsAllValues(dst.store, src.store);
    }
}
