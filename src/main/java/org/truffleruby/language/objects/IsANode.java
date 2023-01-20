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

import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class IsANode extends RubyBaseNode {

    @NeverDefault
    public static IsANode create() {
        return IsANodeGen.create();
    }

    public static IsANode getUncached() {
        return IsANodeGen.getUncached();
    }

    public abstract boolean executeIsA(Object self, RubyModule module);

    @Specialization(
            guards = {
                    "isSingleContext()",
                    "metaClassNode.execute(self) == cachedMetaClass",
                    "module == cachedModule" },
            assumptions = "getHierarchyUnmodifiedAssumption(cachedModule)",
            limit = "getCacheLimit()")
    protected boolean isAMetaClassCached(Object self, RubyModule module,
            @Cached MetaClassNode metaClassNode,
            @Cached("metaClassNode.execute(self)") RubyClass cachedMetaClass,
            @Cached("module") RubyModule cachedModule,
            @Cached("isA(cachedMetaClass, cachedModule)") boolean result) {
        return result;
    }

    public Assumption getHierarchyUnmodifiedAssumption(RubyModule module) {
        return module.fields.getHierarchyUnmodifiedAssumption();
    }

    @Specialization(
            guards = { "isSingleContext()", "klass == cachedClass" },
            replaces = "isAMetaClassCached",
            limit = "getCacheLimit()")
    protected boolean isAClassCached(Object self, RubyClass klass,
            @Cached MetaClassNode metaClassNode,
            @Cached ConditionProfile isMetaClass,
            @Cached("klass") RubyClass cachedClass) {
        return isAClassUncached(self, klass, metaClassNode, isMetaClass);
    }

    @Specialization(replaces = "isAClassCached")
    protected boolean isAClassUncached(Object self, RubyClass klass,
            @Cached @Shared("metaClassNode") MetaClassNode metaClassNode,
            @Cached ConditionProfile isMetaClass) {
        final RubyClass metaclass = metaClassNode.execute(self);

        if (isMetaClass.profile(metaclass == klass)) {
            return true;
        }

        assert metaclass.ancestorClasses != null;

        final int depth = klass.depth;
        if (depth < metaclass.depth) { // < and not <= as we checked for equality above
            return metaclass.ancestorClasses[depth] == klass;
        }

        return false;
    }

    @Specialization(guards = "!isRubyClass(module)", replaces = "isAMetaClassCached")
    protected boolean isAUncached(Object self, RubyModule module,
            @Cached @Shared("metaClassNode") MetaClassNode metaClassNode) {
        return isA(metaClassNode.execute(self), module);
    }

    @TruffleBoundary
    protected boolean isA(RubyClass metaClass, RubyModule module) {
        return ModuleOperations.assignableTo(metaClass, module);
    }

    protected int getCacheLimit() {
        return getLanguage().options.IS_A_CACHE;
    }

}
