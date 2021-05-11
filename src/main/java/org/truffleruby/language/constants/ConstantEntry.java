/*
 * Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.Truffle;
import org.truffleruby.language.RubyConstant;

public class ConstantEntry {

    private final Assumption assumption;
    private final RubyConstant constant;

    public ConstantEntry() {
        this.assumption = Truffle.getRuntime().createAssumption("constant is not defined:");
        this.constant = null;
    }

    public ConstantEntry(RubyConstant constant) {
        this.assumption = Truffle.getRuntime().createAssumption("constant is not overridden");
        this.constant = constant;
    }

    public Assumption getAssumption() {
        return assumption;
    }

    public RubyConstant getConstant() {
        return constant;
    }

    public void invalidate(String message) {
        assumption.invalidate(message);
    }

}
