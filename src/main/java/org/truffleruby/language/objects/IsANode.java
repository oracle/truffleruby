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

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@ReportPolymorphism
@GenerateUncached
public abstract class IsANode extends RubyBaseWithoutContextNode {

    public static IsANode create() {
        return IsANodeGen.create();
    }

    public abstract boolean executeIsA(Object self, DynamicObject module);

    @Specialization(guards = {
            "isRubyModule(cachedModule)",
            "metaClassNode.executeMetaClass(self) == cachedMetaClass",
            "module == cachedModule"
    }, assumptions = "getHierarchyUnmodifiedAssumption(cachedModule)", limit = "getCacheLimit()")
    protected boolean isACached(Object self, DynamicObject module,
            @Cached MetaClassNode metaClassNode,
            @Cached("metaClassNode.executeMetaClass(self)") DynamicObject cachedMetaClass,
            @Cached("module") DynamicObject cachedModule,
            @Cached("isA(cachedMetaClass, cachedModule)") boolean result) {
        return result;
    }

    public Assumption getHierarchyUnmodifiedAssumption(DynamicObject module) {
        return Layouts.MODULE.getFields(module).getHierarchyUnmodifiedAssumption();
    }

    @Specialization(guards = "isRubyModule(module)", replaces = "isACached")
    protected boolean isAUncached(Object self, DynamicObject module,
            @Cached MetaClassNode metaClassNode) {
        return isA(metaClassNode.executeMetaClass(self), module);
    }

    @Specialization(guards = "!isRubyModule(module)")
    protected boolean isATypeError(Object self, DynamicObject module,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(context, context.getCoreExceptions().typeError("class or module required", this));
    }

    @TruffleBoundary
    protected boolean isA(DynamicObject metaClass, DynamicObject module) {
        return ModuleOperations.assignableTo(metaClass, module);
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().IS_A_CACHE;
    }

}
