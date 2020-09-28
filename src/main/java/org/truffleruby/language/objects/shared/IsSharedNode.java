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
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ShapeCachingGuards;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

@ImportStatic(ShapeCachingGuards.class)
@GenerateUncached
public abstract class IsSharedNode extends RubyBaseNode {

    protected static final int CACHE_LIMIT = 8;

    public abstract boolean executeIsShared(RubyDynamicObject object);

    public static IsSharedNode create() {
        return IsSharedNodeGen.create();
    }

    @Specialization(
            guards = { "object.getShape() == cachedShape", "contextReference.get() == cachedContext" },
            assumptions = "cachedShape.getValidAssumption()",
            limit = "CACHE_LIMIT")
    protected boolean isShareCached(RubyDynamicObject object,
            @Cached("object.getShape()") Shape cachedShape,
            @CachedContext(RubyLanguage.class) TruffleLanguage.ContextReference<RubyContext> contextReference,
            @Cached("contextReference.get()") RubyContext cachedContext,
            @Cached("cachedShape.isShared()") boolean shared) {
        return shared;
    }

    @Specialization(guards = "updateShape(object)")
    protected boolean updateShapeAndIsShared(RubyDynamicObject object) {
        return executeIsShared(object);
    }

    @Specialization(replaces = { "isShareCached", "updateShapeAndIsShared" })
    protected boolean isSharedUncached(RubyDynamicObject object,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getOptions().SHARED_OBJECTS_ENABLED && SharedObjects.isShared(object);
    }

}
