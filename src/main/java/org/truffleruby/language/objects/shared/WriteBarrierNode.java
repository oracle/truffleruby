/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.DataObjectFinalizerReference;
import org.truffleruby.core.FinalizerReference;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ShapeCachingGuards;

import java.util.ArrayList;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

@ImportStatic(ShapeCachingGuards.class)
@GenerateUncached
@GenerateInline(inlineByDefault = true)
public abstract class WriteBarrierNode extends RubyBaseNode {

    public static final WriteBarrierNode[] EMPTY_ARRAY = new WriteBarrierNode[0];

    protected static final int MAX_DEPTH = 3;

    protected final void execute(Node node, Object value, int depth) {
        CompilerAsserts.partialEvaluationConstant(depth);
        executeInternal(node, value, depth);
    }

    public final void execute(Node node, Object value) {
        execute(node, value, 0);
    }

    public final void executeCached(Object value, int depth) {
        execute(this, value, depth);
    }

    protected abstract void executeInternal(Node node, Object value, int depth);

    @Specialization(guards = { "!isRubyDynamicObject(value)", "!isFinalizer(value)" })
    protected static void noWriteBarrier(Node node, Object value, int depth) {
    }

    @Specialization(
            guards = { "value.getShape() == cachedShape", "cachedShape.isShared()" },
            limit = "1") // limit of 1 as the next specialization is cheap
    protected static void alreadySharedCached(RubyDynamicObject value, int depth,
            @Cached("value.getShape()") Shape cachedShape) {
    }

    @Specialization(guards = "value.getShape().isShared()", replaces = "alreadySharedCached")
    protected static void alreadySharedUncached(RubyDynamicObject value, int depth) {
    }

    @Specialization(
            guards = { "depth < MAX_DEPTH", "value.getShape() == cachedShape", "!cachedShape.isShared()" },
            assumptions = "cachedShape.getValidAssumption()",
            // limit of 1 to avoid creating many nodes if the value's Shape is polymorphic.
            limit = "1")
    protected static void writeBarrierCached(RubyDynamicObject value, int depth,
            @Cached("value.getShape()") Shape cachedShape,
            @Cached("createShareObjectNode(depth)") ShareObjectNode shareObjectNode) {
        shareObjectNode.executeShare(value);
    }

    @Specialization(guards = "updateShape(value)")
    protected static void updateShapeAndWriteBarrier(RubyDynamicObject value, int depth,
            @Cached(inline = false) WriteBarrierNode writeBarrierNode) {
        writeBarrierNode.executeCached(value, depth);
    }

    @Specialization(guards = "!value.getShape().isShared()",
            replaces = { "writeBarrierCached", "updateShapeAndWriteBarrier" })
    protected static void writeBarrierUncached(Node node, RubyDynamicObject value, int depth) {
        SharedObjects.writeBarrier(getLanguage(node), value);
    }

    @Specialization
    @TruffleBoundary
    protected static void writeBarrierFinalizer(Node node, FinalizerReference ref, int depth) {
        ArrayList<Object> roots = new ArrayList<>();
        ref.collectRoots(roots);
        for (var root : roots) {
            SharedObjects.writeBarrier(getLanguage(node), root);
        }
    }


    @Specialization
    @TruffleBoundary
    protected static void writeBarrierDataFinalizer(Node node, DataObjectFinalizerReference ref, int depth) {
        SharedObjects.writeBarrier(getLanguage(node), ref.dataHolder);
    }

    protected static boolean isFinalizer(Object object) {
        return object instanceof FinalizerReference || object instanceof DataObjectFinalizerReference;
    }

    protected static ShareObjectNode createShareObjectNode(int depth) {
        return ShareObjectNodeGen.create(depth + 1);
    }

}
