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

import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayAppendManyNode extends RubyBaseNode {

    @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

    public abstract RubyArray executeAppendMany(RubyArray array, RubyArray other);

    // Append of a compatible type

    /** Appending an empty array is a no-op, and shouldn't cause an immutable array store to be converted into a mutable
     * one unnecessarily. */
    @Specialization(guards = "isEmptyArray(other)")
    protected RubyArray appendZero(RubyArray array, RubyArray other) {
        return array;
    }

    @Specialization(
            guards = { "!isEmptyArray(other)", "stores.acceptsAllValues(array.store, other.store)" },
            limit = "storageStrategyLimit()")
    protected RubyArray appendManySameType(RubyArray array, RubyArray other,
            @Bind("array.store") Object store,
            @Bind("other.store") Object otherStore,
            @CachedLibrary("store") ArrayStoreLibrary stores,
            @CachedLibrary("otherStore") ArrayStoreLibrary otherStores,
            @Cached ConditionProfile extendProfile) {
        final int oldSize = array.size;
        final int otherSize = other.size;
        final int newSize = oldSize + otherSize;
        final int length = stores.capacity(store);

        propagateSharingNode.executePropagate(array, other);
        if (extendProfile.profile(newSize > length)) {
            final int capacity = ArrayUtils.capacity(getLanguage(), length, newSize);
            Object newStore = stores.expand(store, capacity);
            otherStores.copyContents(otherStore, 0, newStore, oldSize, otherSize);
            setStoreAndSize(array, newStore, newSize);
        } else {
            otherStores.copyContents(otherStore, 0, store, oldSize, otherSize);
            setSize(array, newSize);
        }
        return array;
    }

    // Generalizations

    @Specialization(
            guards = { "!isEmptyArray(other)", "!stores.acceptsAllValues(array.store, other.store)" },
            limit = "storageStrategyLimit()")
    protected RubyArray appendManyGeneralize(RubyArray array, RubyArray other,
            @Bind("array.store") Object store,
            @Bind("other.store") Object otherStore,
            @CachedLibrary("store") ArrayStoreLibrary stores,
            @CachedLibrary("otherStore") ArrayStoreLibrary otherStores) {
        final int oldSize = array.size;
        final int otherSize = other.size;
        final int newSize = oldSize + otherSize;
        final Object newStore = stores.allocateForNewStore(store, otherStore, newSize);

        propagateSharingNode.executePropagate(array, other);
        stores.copyContents(store, 0, newStore, 0, oldSize);
        otherStores.copyContents(otherStore, 0, newStore, oldSize, otherSize);
        setStoreAndSize(array, newStore, newSize);
        return array;
    }
}
