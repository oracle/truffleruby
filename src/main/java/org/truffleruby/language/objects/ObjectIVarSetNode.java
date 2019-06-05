/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.objects.shared.SharedObjects;

@ReportPolymorphism
@GenerateUncached
public abstract class ObjectIVarSetNode extends Node {

    public static ObjectIVarSetNode create() {
        return ObjectIVarSetNodeGen.create();
    }

    public abstract Object executeIVarSet(DynamicObject object, Object name, Object value);

    @Specialization(guards = "name == cachedName", limit = "getCacheLimit()")
    public Object ivarSetCached(DynamicObject object, Object name, Object value,
            @Cached("name") Object cachedName,
            @Cached("create(cachedName)") WriteObjectFieldNode writeObjectFieldNode) {
        writeObjectFieldNode.write(object, value);
        return value;
    }

    @TruffleBoundary
    @Specialization(replaces = "ivarSetCached")
    public Object ivarSetUncached(DynamicObject object, Object name, Object value,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
        if (SharedObjects.isShared(rubyContext, object)) {
            SharedObjects.writeBarrier(rubyContext, value);
            synchronized (object) {
                object.define(name, value);
            }
        } else {
            object.define(name, value);
        }
        return value;
    }

    protected int getCacheLimit() {
        // TODO (pitr-ch 05-Jun-2019): ok?
        return RubyLanguage.getCurrentContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }

}
