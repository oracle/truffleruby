/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.HostCompilerDirectives.InliningCutoff;
import com.oracle.truffle.api.dsl.NeverDefault;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class MetaClassNode extends RubyBaseNode {

    @NeverDefault
    public static MetaClassNode create() {
        return MetaClassNodeGen.create();
    }

    public static MetaClassNode getUncached() {
        return MetaClassNodeGen.getUncached();
    }

    public abstract RubyClass execute(Object value);

    @Specialization(guards = "isPrimitiveOrImmutable(value)")
    protected RubyClass metaClassImmutable(Object value,
            @Cached ImmutableClassNode immutableClassNode) {
        return immutableClassNode.execute(this, value);
    }

    // Cover all RubyDynamicObject cases with cached and uncached

    @Specialization(
            guards = { "object == cachedObject", "metaClass.isSingleton" },
            limit = "getIdentityCacheContextLimit()")
    protected RubyClass singletonClassCached(RubyDynamicObject object,
            @Cached("object") RubyDynamicObject cachedObject,
            @Cached("object.getMetaClass()") RubyClass metaClass) {
        return metaClass;
    }

    @Specialization(replaces = "singletonClassCached")
    protected RubyClass metaClassObject(RubyDynamicObject object) {
        return object.getMetaClass();
    }

    // Foreign object
    @InliningCutoff
    @Specialization(guards = "isForeignObject(object)")
    protected RubyClass metaClassForeign(Object object,
            @Cached ForeignClassNode foreignClassNode) {
        return foreignClassNode.execute(object);
    }

    protected int getCacheLimit() {
        return getLanguage().options.CLASS_CACHE;
    }
}
