/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import static org.truffleruby.core.array.ArrayHelpers.setSize;
import static org.truffleruby.core.array.ArrayHelpers.setStoreAndSize;

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;

@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayWriteNormalizedNode extends RubyBaseNode {

    @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

    public abstract Object executeWrite(RubyArray array, int index, Object value);

    // Writing within an existing array with a compatible type

    @Specialization(
            guards = { "isInBounds(array, index)", "stores.acceptsValue(store, value)" },
            limit = "storageStrategyLimit()")
    protected Object writeWithin(RubyArray array, int index, Object value,
            @Bind("array.store") Object store,
            @CachedLibrary("store") ArrayStoreLibrary stores) {
        propagateSharingNode.executePropagate(array, value);
        stores.write(store, index, value);
        return value;
    }

    // Writing within an existing array with an incompatible type - need to generalise

    @Specialization(
            guards = {
                    "isInBounds(array, index)",
                    "!stores.acceptsValue(store, value)"
            },
            limit = "storageStrategyLimit()")
    protected Object writeWithinGeneralizeNonMutable(RubyArray array, int index, Object value,
            @Bind("array.store") Object store,
            @CachedLibrary("store") ArrayStoreLibrary stores,
            @CachedLibrary(limit = "1") ArrayStoreLibrary newStores) {
        final int size = array.size;
        final Object newStore = stores.allocateForNewValue(store, value, size);
        stores.copyContents(store, 0, newStore, 0, size);
        propagateSharingNode.executePropagate(array, value);
        newStores.write(newStore, index, value);
        array.store = newStore;
        return value;
    }

    // Extending an array of compatible type by just one

    @Specialization(guards = "isExtendingByOne(array, index)")
    protected Object writeExtendByOne(RubyArray array, int index, Object value,
            @Cached ArrayAppendOneNode appendNode) {
        appendNode.executeAppendOne(array, value);
        return value;
    }

    // Writing beyond the end of an array - may need to generalize to Object[] or otherwise extend
    @Specialization(
            guards = {
                    "!isInBounds(array, index)",
                    "!isExtendingByOne(array, index)",
                    "stores.isPrimitive(store)" },
            limit = "storageStrategyLimit()")
    protected Object writeBeyondPrimitive(RubyArray array, int index, Object value,
            @Bind("array.store") Object store,
            @CachedLibrary("store") ArrayStoreLibrary stores,
            @CachedLibrary(limit = "1") ArrayStoreLibrary newStores,
            @Cached LoopConditionProfile loopProfile) {
        final int newSize = index + 1;
        final Object objectStore = stores.allocateForNewValue(store, nil, newSize);
        int oldSize = array.size;
        stores.copyContents(store, 0, objectStore, 0, oldSize);
        int n = oldSize;
        try {
            for (; loopProfile.inject(n < index); n++) {
                newStores.write(objectStore, n, nil);
                TruffleSafepoint.poll(this);
            }
        } finally {
            profileAndReportLoopCount(loopProfile, n - oldSize);
        }
        propagateSharingNode.executePropagate(array, value);
        newStores.write(objectStore, index, value);
        setStoreAndSize(array, objectStore, newSize);
        return value;
    }

    @Specialization(
            guards = {
                    "!isInBounds(array, index)",
                    "!isExtendingByOne(array, index)",
                    "!stores.isPrimitive(store)" },
            limit = "storageStrategyLimit()")
    protected Object writeBeyondObject(RubyArray array, int index, Object value,
            @Bind("array.store") Object store,
            @CachedLibrary("store") ArrayStoreLibrary stores,
            @CachedLibrary(limit = "1") ArrayStoreLibrary newStores,
            @Cached ArrayEnsureCapacityNode ensureCapacityNode,
            @Cached LoopConditionProfile loopProfile) {
        Object newStore = ensureCapacityNode.executeEnsureCapacity(array, index + 1);
        int n = array.size;
        try {
            for (; loopProfile.inject(n < index); n++) {
                newStores.write(newStore, n, nil);
                TruffleSafepoint.poll(this);
            }
        } finally {
            profileAndReportLoopCount(loopProfile, n - array.size);
        }
        propagateSharingNode.executePropagate(array, value);
        newStores.write(newStore, index, value);
        setSize(array, index + 1);
        return value;
    }

    // Guards

    protected static boolean isInBounds(RubyArray array, int index) {
        return index >= 0 && index < array.size;
    }

    protected static boolean isExtendingByOne(RubyArray array, int index) {
        return index == array.size;
    }

}
