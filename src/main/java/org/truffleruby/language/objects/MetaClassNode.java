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
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

// Specializations are order by their frequency on railsbench using --engine.SpecializationStatistics
@GenerateUncached
@GenerateInline(inlineByDefault = true)
public abstract class MetaClassNode extends RubyBaseNode {

    @NeverDefault
    public static MetaClassNode create() {
        return MetaClassNodeGen.create();
    }

    public static RubyClass executeUncached(Object value) {
        return MetaClassNodeGen.getUncached().execute(null, value);
    }

    public final RubyClass executeCached(Object value) {
        return execute(this, value);
    }

    public abstract RubyClass execute(Node node, Object value);

    @Specialization(
            guards = { "isSingleContext()", "object == cachedObject", "metaClass.isSingleton" },
            limit = "1")
    protected RubyClass singleton(RubyDynamicObject object,
            @Cached("object") RubyDynamicObject cachedObject,
            @Cached("object.getMetaClass()") RubyClass metaClass) {
        return metaClass;
    }

    @Specialization(replaces = "singleton")
    protected RubyClass object(RubyDynamicObject object) {
        return object.getMetaClass();
    }

    @Specialization(guards = "isPrimitiveOrImmutable(value)")
    protected static RubyClass immutable(Node node, Object value,
            @Cached ImmutableClassNode immutableClassNode) {
        return immutableClassNode.execute(node, value);
    }

    @InliningCutoff
    @Specialization(guards = "isForeignObject(object)")
    protected static RubyClass foreign(Node node, Object object,
            @Cached ForeignClassNode foreignClassNode) {
        return foreignClassNode.execute(node, object);
    }

    protected int getCacheLimit() {
        return getLanguage().options.CLASS_CACHE;
    }
}
