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

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ShapeCachingGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

@ImportStatic(ShapeCachingGuards.class)
@GenerateUncached
@NodeField(name = "depth", type = int.class)
public abstract class WriteBarrierNode extends RubyBaseNode {

    protected static final int CACHE_LIMIT = 8;
    protected static final int MAX_DEPTH = 3;

    protected abstract int getDepth();

    public static WriteBarrierNode create() {
        return WriteBarrierNodeGen.create(0);
    }

    public abstract void executeWriteBarrier(Object value);

    @Specialization(
            guards = { "value.getShape() == cachedShape", "getDepth() < MAX_DEPTH" },
            assumptions = "cachedShape.getValidAssumption()",
            limit = "CACHE_LIMIT")
    protected void writeBarrierCached(RubyDynamicObject value,
            @Cached("value.getShape()") Shape cachedShape,
            @Cached("cachedShape.isShared()") boolean alreadyShared,
            @Cached("createShareObjectNode(alreadyShared)") ShareObjectNode shareObjectNode) {
        if (!alreadyShared) {
            shareObjectNode.executeShare(value);
        }
    }

    @Specialization(guards = "updateShape(value)")
    protected void updateShapeAndWriteBarrier(RubyDynamicObject value) {
        executeWriteBarrier(value);
    }

    @Specialization(replaces = { "writeBarrierCached", "updateShapeAndWriteBarrier" })
    protected void writeBarrierUncached(RubyDynamicObject value) {
        SharedObjects.writeBarrier(getLanguage(), value);
    }

    @Specialization(guards = "!isRubyDynamicObject(value)")
    protected void noWriteBarrier(Object value) {
    }

    protected ShareObjectNode createShareObjectNode(boolean alreadyShared) {
        if (!alreadyShared) {
            return ShareObjectNodeGen.create(getDepth() + 1);
        } else {
            return null;
        }
    }

}
