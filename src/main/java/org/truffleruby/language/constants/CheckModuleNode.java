/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

/**
 * An efficient way to check if the value is a module. Needed for A::B constant lookup to check that
 * A is a module, see {@link ReadConstantNode}.
 */
public abstract class CheckModuleNode extends RubyBaseNode {

    public abstract DynamicObject executeCheckModule(Object module);

    @Specialization(guards = { "module == cachedModule", "isRubyModule(cachedModule)" }, limit = "getCacheLimit()")
    protected DynamicObject checkModule(Object module,
            @Cached("module") Object cachedModule) {
        return (DynamicObject) cachedModule;
    }

    @Specialization
    protected DynamicObject checkModuleUncached(Object module,
            @Cached BranchProfile notModuleProfile) {
        if (RubyGuards.isRubyModule(module)) {
            return (DynamicObject) module;
        } else {
            notModuleProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorIsNotAClassModule(module, this));
        }
    }

    protected int getCacheLimit() {
        return getContext().getOptions().CONSTANT_CACHE;
    }

}
