/*
 * Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.Truffle;
import org.truffleruby.language.methods.SharedMethodInfo;

public final class MethodEntry {

    private final Assumption assumption;
    private final InternalMethod method;

    public MethodEntry(InternalMethod method) {
        assert method != null;
        this.assumption = Truffle.getRuntime().createAssumption("method is not overridden:");
        this.method = method;
    }

    public MethodEntry() {
        this.assumption = Truffle.getRuntime().createAssumption("method is not defined:");
        this.method = null;
    }

    public MethodEntry withNewAssumption() {
        if (method != null) {
            return new MethodEntry(method);
        } else {
            return new MethodEntry();
        }
    }

    public Assumption getAssumption() {
        return assumption;
    }

    public InternalMethod getMethod() {
        return method;
    }

    public void invalidate(RubyModule module, String methodName) {
        assumption.invalidate(SharedMethodInfo.moduleAndMethodName(module, methodName));
    }

}
