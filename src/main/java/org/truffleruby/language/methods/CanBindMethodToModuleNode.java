/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

/** Caches {@link ModuleOperations#canBindMethodTo} for a method. */
public abstract class CanBindMethodToModuleNode extends RubyBaseNode {

    public static CanBindMethodToModuleNode create() {
        return CanBindMethodToModuleNodeGen.create();
    }

    public abstract boolean executeCanBindMethodToModule(InternalMethod method, RubyModule module);

    @Specialization(
            guards = {
                    "method.getDeclaringModule() == declaringModule",
                    "module == cachedModule" },
            limit = "getCacheLimit()")
    protected boolean canBindMethodToCached(InternalMethod method, RubyModule module,
            @Cached("method.getDeclaringModule()") RubyModule declaringModule,
            @Cached("module") RubyModule cachedModule,
            @Cached("canBindMethodTo(method, cachedModule)") boolean canBindMethodTo) {
        return canBindMethodTo;
    }

    @Specialization
    protected boolean canBindMethodToUncached(InternalMethod method, RubyModule module) {
        return canBindMethodTo(method, module);
    }

    protected boolean canBindMethodTo(InternalMethod method, RubyModule module) {
        return ModuleOperations.canBindMethodTo(method, module);
    }

    protected int getCacheLimit() {
        return getLanguage().options.BIND_CACHE;
    }

}
