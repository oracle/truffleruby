/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Caches {@link ModuleOperations#canBindMethodTo} for a method.
 */
public abstract class CanBindMethodToModuleNode extends RubyBaseNode {

    public static CanBindMethodToModuleNode create() {
        return CanBindMethodToModuleNodeGen.create();
    }

    public abstract boolean executeCanBindMethodToModule(InternalMethod method, DynamicObject module);

    @Specialization(guards = { "isRubyModule(module)", "method.getDeclaringModule() == declaringModule", "module == cachedModule" }, limit = "getCacheLimit()")
    protected boolean canBindMethodToCached(InternalMethod method, DynamicObject module,
            @Cached("method.getDeclaringModule()") DynamicObject declaringModule,
            @Cached("module") DynamicObject cachedModule,
            @Cached("canBindMethodTo(method, cachedModule)") boolean canBindMethodTo) {
        return canBindMethodTo;
    }

    @Specialization(guards = "isRubyModule(module)")
    protected boolean canBindMethodToUncached(InternalMethod method, DynamicObject module) {
        return canBindMethodTo(method, module);
    }

    protected boolean canBindMethodTo(InternalMethod method, DynamicObject module) {
        return ModuleOperations.canBindMethodTo(method, module);
    }

    protected int getCacheLimit() {
        return getContext().getOptions().BIND_CACHE;
    }

}
