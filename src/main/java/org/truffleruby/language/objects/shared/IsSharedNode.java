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

import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ShapeCachingGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

@ImportStatic(ShapeCachingGuards.class)
@GenerateUncached
@GenerateInline(inlineByDefault = true)
public abstract class IsSharedNode extends RubyBaseNode {

    protected static final int CACHE_LIMIT = 8;

    public abstract boolean executeIsShared(Node node, RubyDynamicObject object);

    @Specialization(
            guards = "object.getShape() == cachedShape",
            assumptions = "cachedShape.getValidAssumption()",
            limit = "CACHE_LIMIT")
    protected static boolean isShareCached(RubyDynamicObject object,
            @Cached("object.getShape()") Shape cachedShape,
            @Cached("cachedShape.isShared()") boolean shared) {
        return shared;
    }

    @Specialization(guards = "updateShape(object)")
    protected static boolean updateShapeAndIsShared(Node node, RubyDynamicObject object,
            @Cached(inline = false) IsSharedNode isSharedNode) {
        return isSharedNode.executeIsShared(node, object);
    }

    @Specialization(replaces = { "isShareCached", "updateShapeAndIsShared" })
    protected static boolean isSharedUncached(Node node, RubyDynamicObject object) {
        return getLanguage(node).options.SHARED_OBJECTS_ENABLED && SharedObjects.isShared(object);
    }

}
