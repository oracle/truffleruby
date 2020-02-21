/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import static org.truffleruby.core.array.ArrayHelpers.getSize;
import static org.truffleruby.core.array.ArrayHelpers.getStore;
import static org.truffleruby.core.array.ArrayHelpers.setSize;
import static org.truffleruby.core.array.ArrayHelpers.setStoreAndSize;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;

@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayWriteNormalizedNode extends RubyContextNode {

    @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

    public abstract Object executeWrite(DynamicObject array, int index, Object value);

    // Writing within an existing array with a compatible type

    @Specialization(
            guards = { "isInBounds(array, index)", "arrays.acceptsValue(getStore(array), value)" },
            limit = "STORAGE_STRATEGIES")
    protected Object writeWithin(DynamicObject array, int index, Object value,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays) {
        propagateSharingNode.executePropagate(array, value);
        arrays.write(getStore(array), index, value);
        return value;
    }

    // Writing within an existing array with an incompatible type - need to generalise

    @Specialization(
            guards = {
                    "isInBounds(array, index)",
                    "!arrays.acceptsValue(getStore(array), value)"
            },
            limit = "STORAGE_STRATEGIES")
    protected Object writeWithinGeneralizeNonMutable(DynamicObject array, int index, Object value,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
            @CachedLibrary(limit = "1") ArrayStoreLibrary newArrays) {
        final int size = getSize(array);
        final Object store = getStore(array);
        final Object newStore = arrays.generalizeForValue(store, value).allocate(size);
        arrays.copyContents(store, 0, newStore, 0, size);
        propagateSharingNode.executePropagate(array, value);
        newArrays.write(newStore, index, value);
        Layouts.ARRAY.setStore(array, newStore);
        return value;
    }

    // Extending an array of compatible type by just one

    @Specialization(guards = "isExtendingByOne(array, index)")
    protected Object writeExtendByOne(DynamicObject array, int index, Object value,
            @Cached ArrayAppendOneNode appendNode) {
        appendNode.executeAppendOne(array, value);
        return value;
    }

    // Writing beyond the end of an array - may need to generalize to Object[] or otherwise extend
    @Specialization(
            guards = {
                    "!isInBounds(array, index)",
                    "!isExtendingByOne(array, index)",
                    "!arrays.allocator(getStore(array)).accepts(nil)" },
            limit = "STORAGE_STRATEGIES")
    protected Object writeBeyondPrimitive(DynamicObject array, int index, Object value,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
            @CachedLibrary(limit = "1") ArrayStoreLibrary newArrays) {
        final int newSize = index + 1;
        Object store = getStore(array);
        final Object objectStore = arrays.generalizeForValue(store, nil).allocate(newSize);
        int oldSize = getSize(array);
        arrays.copyContents(store, 0, objectStore, 0, oldSize);
        for (int n = oldSize; n < index; n++) {
            newArrays.write(objectStore, n, nil);
        }
        propagateSharingNode.executePropagate(array, value);
        newArrays.write(objectStore, index, value);
        setStoreAndSize(array, objectStore, newSize);
        return value;
    }

    @Specialization(
            guards = {
                    "!isInBounds(array, index)",
                    "!isExtendingByOne(array, index)",
                    "arrays.allocator(getStore(array)).accepts(nil)" },
            limit = "STORAGE_STRATEGIES")
    protected Object writeBeyondObject(DynamicObject array, int index, Object value,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
            @CachedLibrary(limit = "1") ArrayStoreLibrary newArrays,
            @Cached ArrayEnsureCapacityNode ensureCapacityNode) {
        ensureCapacityNode.executeEnsureCapacity(array, index + 1);
        final Object store = getStore(array);
        for (int n = getSize(array); n < index; n++) {
            newArrays.write(store, n, nil);
        }
        propagateSharingNode.executePropagate(array, value);
        newArrays.write(store, index, value);
        setSize(array, index + 1);
        return value;
    }

    // Guards

    protected static boolean isInBounds(DynamicObject array, int index) {
        return index >= 0 && index < getSize(array);
    }

    protected static boolean isExtendingByOne(DynamicObject array, int index) {
        return index == getSize(array);
    }

}
