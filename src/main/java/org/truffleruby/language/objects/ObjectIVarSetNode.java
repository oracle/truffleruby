/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@ReportPolymorphism
@GenerateUncached
public abstract class ObjectIVarSetNode extends RubyBaseWithoutContextNode {

    public static ObjectIVarSetNode create() {
        return ObjectIVarSetNodeGen.create();
    }

    public abstract Object executeIVarSet(DynamicObject object, Object name, Object value, boolean checkName);

    public final Object executeIVarSet(DynamicObject object, Object name, Object value) {
        return executeIVarSet(object, name, value, false);
    }

    @Specialization(guards = { "name == cachedName", "checkName == cachedCheckName" }, limit = "getCacheLimit()")
    protected Object ivarSetCached(DynamicObject object, Object name, Object value, boolean checkName,
            @Cached("checkName") boolean cachedCheckName,
            @CachedContext(RubyLanguage.class) RubyContext context,
            // context does not have to be guarded since it used only during cache instance creation
            @Cached("checkName(context, object, name, cachedCheckName)") Object cachedName,
            @Cached WriteObjectFieldNode writeObjectFieldNode) {
        writeObjectFieldNode.write(object, cachedName, value);
        return value;
    }

    @TruffleBoundary
    @Specialization(replaces = "ivarSetCached")
    protected Object ivarSetUncached(DynamicObject object, Object name, Object value, boolean checkName,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        if (SharedObjects.isShared(context, object)) {
            SharedObjects.writeBarrier(context, value);
            synchronized (object) {
                object.define(checkName(context, object, name, checkName), value);
            }
        } else {
            object.define(checkName(context, object, name, checkName), value);
        }
        return value;
    }

    protected Object checkName(
            RubyContext context, DynamicObject object, Object name, boolean checkName) {
        return ObjectIVarGetNode.checkName(this, context, object, name, checkName);
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }

}
