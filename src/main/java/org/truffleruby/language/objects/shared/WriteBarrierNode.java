/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.ShapeCachingGuards;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

@ImportStatic(ShapeCachingGuards.class)
@GenerateUncached
public abstract class WriteBarrierNode extends RubyBaseNode {

    protected static final int CACHE_LIMIT = 8;
    protected static final int MAX_DEPTH = 3;

    public static WriteBarrierNode create() {
        return WriteBarrierNodeGen.create();
    }

    public final void executeWriteBarrier(Object value) {
        executeWriteBarrier(value, 0);
    }

    public abstract void executeWriteBarrier(Object value, int depth);

    @Specialization(
            guards = {
                    "value.getShape() == cachedShape",
                    "depth < MAX_DEPTH", // TODO (pitr-ch 24-Jan-2020): fix check in interpreter
                    "contextReference.get() == cachedContext" },
            assumptions = "cachedShape.getValidAssumption()",
            limit = "CACHE_LIMIT")
    protected void writeBarrierCached(DynamicObject value, int depth,
            @Cached("value.getShape()") Shape cachedShape,
            @CachedContext(RubyLanguage.class) TruffleLanguage.ContextReference<RubyContext> contextReference,
            @Cached("contextReference.get()") RubyContext cachedContext,
            @Cached("isShared(cachedContext, cachedShape)") boolean alreadyShared,
            @Cached("createShareObjectNode(alreadyShared, depth)") ShareObjectNode shareObjectNode) {
        if (!alreadyShared) {
            shareObjectNode.executeShare(value);
        }
    }

    @Specialization(guards = "updateShape(value)")
    protected void updateShapeAndWriteBarrier(DynamicObject value, int depth) {
        executeWriteBarrier(value, depth);
    }

    @Specialization(replaces = { "writeBarrierCached", "updateShapeAndWriteBarrier" })
    protected void writeBarrierUncached(DynamicObject value, int depth,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        SharedObjects.writeBarrier(context, value);
    }

    @Specialization(guards = "!isDynamicObject(value)")
    protected void noWriteBarrier(Object value, int depth) {
    }

    protected boolean isShared(RubyContext context, Shape shape) {
        return SharedObjects.isShared(context, shape);
    }

    protected ShareObjectNode createShareObjectNode(boolean alreadyShared, int depth) {
        if (!alreadyShared) {
            return ShareObjectNodeGen.create(depth + 1);
        } else {
            return null;
        }
    }

}
