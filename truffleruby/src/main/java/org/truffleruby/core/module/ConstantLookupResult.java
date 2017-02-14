/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.module;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;

public class ConstantLookupResult {

    private final RubyConstant constant;
    private final Assumption[] assumptions;

    public ConstantLookupResult(RubyConstant constant, Assumption... assumptions) {
        this.constant = constant;
        this.assumptions = assumptions;
    }

    public boolean isFound() {
        return constant != null;
    }

    public boolean isDeprecated() {
        return constant != null && constant.isDeprecated();
    }

    public boolean isVisibleTo(RubyContext context, LexicalScope lexicalScope, DynamicObject module) {
        return constant == null || constant.isVisibleTo(context, lexicalScope, module);
    }

    public RubyConstant getConstant() {
        return constant;
    }

    public Assumption[] getAssumptions() {
        return assumptions;
    }

}
