/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.object.DynamicObject;

/**
 * A reference to an included RubyModule.
 */
public class IncludedModule extends ModuleChain {

    private final DynamicObject includedModule;

    public IncludedModule(DynamicObject includedModule, ModuleChain parentModule) {
        super(parentModule);
        assert RubyGuards.isRubyModule(includedModule);
        this.includedModule = includedModule;
    }

    @Override
    public DynamicObject getActualModule() {
        return includedModule;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + includedModule + ")";
    }

}
