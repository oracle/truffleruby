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

@ReportPolymorphism
@GenerateUncached
public abstract class ObjectIVarGetNode extends Node {

    public static ObjectIVarGetNode create() {
        return ObjectIVarGetNodeGen.create();
    }

    public abstract Object executeIVarGet(DynamicObject object, Object name);

    @Specialization(guards = "name == cachedName", limit = "getCacheLimit()")
    public Object ivarGetCached(DynamicObject object, Object name,
            @Cached("name") Object cachedName,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached("createReadFieldNode(cachedName, rubyContext)") ReadObjectFieldNode readObjectFieldNode) {
        return readObjectFieldNode.execute(object);
    }

    @TruffleBoundary
    @Specialization(replaces = "ivarGetCached")
    public Object ivarGetUncached(DynamicObject object, Object name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
        return ReadObjectFieldNode.read(object, name, rubyContext.getCoreLibrary().getNil());
    }

    protected ReadObjectFieldNode createReadFieldNode(Object name, RubyContext rubyContext) {
        return ReadObjectFieldNodeGen.create(name, rubyContext.getCoreLibrary().getNil());
    }

    protected int getCacheLimit() {
        // TODO (pitr-ch 05-Jun-2019): ok?
        return RubyLanguage.getCurrentContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }

}
