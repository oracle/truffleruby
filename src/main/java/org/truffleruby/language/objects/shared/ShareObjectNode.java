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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.oracle.truffle.api.object.PropertyGetter;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ShapeCachingGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.utils.RunTwiceBranchProfile;

/** Share the object and all that is reachable from it (see {@link ObjectGraph#getAdjacentObjects}) */
@ImportStatic(ShapeCachingGuards.class)
public abstract class ShareObjectNode extends RubyBaseNode {

    protected static final int CACHE_LIMIT = 8;

    protected final int depth;

    public ShareObjectNode(int depth) {
        this.depth = depth;
    }

    public abstract void executeShare(RubyDynamicObject object);

    @ExplodeLoop
    @Specialization(
            guards = { "object.getShape() == cachedShape", "propertyGetters.length <= MAX_EXPLODE_SIZE" },
            assumptions = { "cachedShape.getValidAssumption()", "sharedShape.getValidAssumption()" },
            limit = "CACHE_LIMIT")
    protected void shareCached(RubyDynamicObject object,
            @Cached("object.getShape()") Shape cachedShape,
            @Cached("createSharedShape(cachedShape)") Shape sharedShape,
            @CachedLibrary(limit = "1") DynamicObjectLibrary objectLibrary,
            @Cached("new()") RunTwiceBranchProfile shareMetaClassProfile,
            @Cached("createShareInternalFieldsNode()") ShareInternalFieldsNode shareInternalFieldsNode,
            @Cached(value = "getObjectProperties(sharedShape)", dimensions = 1) PropertyGetter[] propertyGetters,
            @Cached("createWriteBarrierNodes(propertyGetters)") WriteBarrierNode[] writeBarrierNodes) {
        // Mark the object as shared first to avoid recursion
        assert object.getShape() == cachedShape;
        objectLibrary.markShared(object);
        assert object.getShape() == sharedShape;

        // Share the metaclass. This will also the share the logical class, which is the same or its superclass.
        // Note that the metaclass might refer to `object` via `attached`, so it is important to share the object first.
        if (!object.getMetaClass().getShape().isShared()) {
            shareMetaClassProfile.enter();
            SharedObjects.writeBarrier(getLanguage(), object.getMetaClass());
        }
        assert SharedObjects
                .isShared(object.getLogicalClass()) : "the logical class should have been shared by the metaclass";

        shareInternalFieldsNode.executeShare(object);

        for (int i = 0; i < propertyGetters.length; i++) {
            final PropertyGetter propertyGetter = propertyGetters[i];
            final Object value = propertyGetter.get(object);
            writeBarrierNodes[i].executeWriteBarrier(value);
        }

        assert allFieldsAreShared(object);
    }

    private boolean allFieldsAreShared(RubyDynamicObject object) {
        for (Object value : ObjectGraph.getAdjacentObjects(object)) {
            assert SharedObjects.isShared(value) : "unshared field in shared object: " + value;
        }

        return true;
    }

    @Specialization(guards = "updateShape(object)")
    protected void updateShapeAndShare(RubyDynamicObject object) {
        executeShare(object);
    }

    @Specialization(replaces = { "shareCached", "updateShapeAndShare" })
    protected void shareUncached(RubyDynamicObject object) {
        SharedObjects.writeBarrier(getLanguage(), object);
    }

    protected static PropertyGetter[] getObjectProperties(Shape shape) {
        final List<PropertyGetter> objectProperties = new ArrayList<>();
        for (Property property : shape.getPropertyListInternal(false)) {
            if (!property.getLocation().isPrimitive()) {
                objectProperties.add(Objects.requireNonNull(shape.makePropertyGetter(property.getKey())));
            }
        }
        return objectProperties.toArray(KernelNodes.CopyInstanceVariablesNode.EMPTY_PROPERTY_GETTER_ARRAY);
    }

    protected ShareInternalFieldsNode createShareInternalFieldsNode() {
        return ShareInternalFieldsNodeGen.create(depth);
    }

    protected WriteBarrierNode[] createWriteBarrierNodes(PropertyGetter[] propertyGetters) {
        WriteBarrierNode[] nodes = propertyGetters.length == 0
                ? WriteBarrierNode.EMPTY_ARRAY
                : new WriteBarrierNode[propertyGetters.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = WriteBarrierNodeGen.create(depth);
        }
        return nodes;
    }

    protected Shape createSharedShape(Shape cachedShape) {
        if (cachedShape.isShared()) {
            throw new UnsupportedOperationException(
                    "Thread-safety bug: the object is already shared. This means another thread marked the object as shared concurrently.");
        } else {
            return cachedShape.makeSharedShape();
        }
    }

}
