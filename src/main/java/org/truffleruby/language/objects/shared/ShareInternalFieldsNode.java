/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import com.oracle.truffle.api.dsl.Cached.Exclusive;
import org.truffleruby.Layouts;
import org.truffleruby.collections.BoundaryIterable;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.array.library.DelegatedArrayStorage;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.queue.RubyQueue;
import org.truffleruby.core.queue.UnsizedQueue;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ShapeCachingGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** Share the internal fields of an object, accessible by its Layout */
@ImportStatic({ ShapeCachingGuards.class, ArrayGuards.class })
public abstract class ShareInternalFieldsNode extends RubyBaseNode {

    protected static final int CACHE_LIMIT = 8;

    protected final int depth;

    public ShareInternalFieldsNode(int depth) {
        this.depth = depth;
    }

    public abstract void executeShare(RubyDynamicObject object);

    @Specialization(guards = "isObjectArray(array)")
    protected void shareCachedObjectArray(RubyArray array,
            @Cached("createWriteBarrierNode()") @Exclusive WriteBarrierNode writeBarrierNode) {
        final int size = array.size;
        final Object[] store = (Object[]) array.store;
        for (int i = 0; i < size; i++) {
            writeBarrierNode.executeWriteBarrier(store[i]);
        }
    }

    @Specialization(guards = "isDelegatedObjectArray(array)")
    protected void shareCachedDelegatedArray(RubyArray array,
            @Cached("createWriteBarrierNode()") @Exclusive WriteBarrierNode writeBarrierNode) {
        final DelegatedArrayStorage delegated = (DelegatedArrayStorage) array.store;
        final Object[] store = (Object[]) delegated.storage;
        for (int i = delegated.offset; i < delegated.offset + delegated.length; i++) {
            writeBarrierNode.executeWriteBarrier(store[i]);
        }
    }

    @Specialization(guards = "stores.isPrimitive(array.store)", limit = "storageStrategyLimit()")
    protected void shareCachedPrimitiveArray(RubyArray array,
            @CachedLibrary("array.store") ArrayStoreLibrary stores) {
        assert ArrayOperations.isPrimitiveStorage(array);
    }

    @Specialization
    protected void shareCachedQueue(RubyQueue object,
            @Cached ConditionProfile profileEmpty,
            @Cached("createWriteBarrierNode()") @Exclusive WriteBarrierNode writeBarrierNode) {
        final UnsizedQueue queue = object.queue;
        if (!profileEmpty.profile(queue.isEmpty())) {
            for (Object e : BoundaryIterable.wrap(queue.getContents())) {
                writeBarrierNode.executeWriteBarrier(e);
            }
        }
    }

    @Specialization(
            guards = { "object.getShape() == cachedShape", "!hasFinalizerRef" },
            assumptions = "cachedShape.getValidAssumption()",
            limit = "CACHE_LIMIT")
    protected void shareCachedBasicObject(RubyBasicObject object,
            @Cached("object.getShape()") Shape cachedShape,
            @Cached("hasFinalizerRefProperty(cachedShape)") boolean hasFinalizerRef) {
        /* No internal fields for RubyBasicObject */
    }

    @Specialization(
            replaces = {
                    "shareCachedObjectArray",
                    "shareCachedDelegatedArray",
                    "shareCachedPrimitiveArray",
                    "shareCachedQueue",
                    "shareCachedBasicObject" })
    protected void shareUncached(RubyDynamicObject object) {
        SharedObjects.shareInternalFields(object);
    }

    protected static boolean isDelegatedObjectArray(RubyArray array) {
        final Object store = array.store;
        return store instanceof DelegatedArrayStorage && ((DelegatedArrayStorage) store).hasObjectArrayStorage();
    }

    protected static boolean hasFinalizerRefProperty(Shape shape) {
        return shape.hasProperty(Layouts.FINALIZER_REF_IDENTIFIER);
    }

    protected WriteBarrierNode createWriteBarrierNode() {
        return WriteBarrierNodeGen.create(depth);
    }

}
