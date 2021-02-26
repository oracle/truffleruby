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

import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.Truffle;

public class MethodEntry {

    private final Assumption assumption;
    private final InternalMethod method;

    public MethodEntry(InternalMethod method) {
        this.assumption = Truffle.getRuntime().createAssumption(method.getName());
        this.method = method;
    }

    public MethodEntry(String name) {
        this.assumption = Truffle.getRuntime().createAssumption(name);
        this.method = null;
    }

    public MethodEntry withNewAssumption() {
        if (method != null) {
            return new MethodEntry(method);
        } else {
            return new MethodEntry(assumption.getName());
        }
    }

    public Assumption getAssumption() {
        return assumption;
    }

    public InternalMethod getMethod() {
        return method;
    }

    public void invalidate() {
        assumption.invalidate();
    }

}
