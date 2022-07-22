/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import com.oracle.truffle.api.dsl.Cached.Exclusive;
import org.truffleruby.collections.BoundaryIterable;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.queue.RubyQueue;
import org.truffleruby.core.queue.UnsizedQueue;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ShapeCachingGuards;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** Share the plain Java fields which may contain objets for subclasses of RubyDynamicObject.
 * {@link RubyDynamicObject#metaClass} is handled by {@link ShareObjectNode}. */
@ImportStatic({ ShapeCachingGuards.class, ArrayGuards.class })
public abstract class ShareInternalFieldsNode extends RubyBaseNode {

    protected static final int CACHE_LIMIT = 8;

    protected final int depth;

    public ShareInternalFieldsNode(int depth) {
        this.depth = depth;
    }

    public abstract void executeShare(RubyDynamicObject object);

    @Specialization(limit = "CACHE_LIMIT")
    protected void shareArray(RubyArray array,
            @Bind("array.getStore()") Object store,
            @CachedLibrary("store") ArrayStoreLibrary stores) {
        array.setStore(stores.makeShared(store));
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

    @Specialization
    protected void shareCachedBasicObject(RubyBasicObject object) {
        /* No extra Java fields for RubyBasicObject */
    }

    @Specialization(
            replaces = {
                    "shareArray",
                    "shareCachedQueue",
                    "shareCachedBasicObject" })
    protected void shareUncached(RubyDynamicObject object) {
        SharedObjects.shareInternalFields(object);
    }

    protected WriteBarrierNode createWriteBarrierNode() {
        return WriteBarrierNodeGen.create(depth);
    }

}
