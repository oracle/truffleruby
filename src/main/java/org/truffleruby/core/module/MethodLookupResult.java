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
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

import org.truffleruby.language.Visibility;
import org.truffleruby.language.methods.InternalMethod;

public class MethodLookupResult {

    private final InternalMethod method;
    @CompilationFinal private final Assumption[] assumptions;

    public MethodLookupResult(InternalMethod method, Assumption... assumptions) {
        this.method = method;
        this.assumptions = assumptions;
    }

    public MethodLookupResult withNoMethod() {
        if (method == null) {
            return this;
        } else {
            return new MethodLookupResult(null, assumptions);
        }
    }

    public boolean isDefined() {
        return method != null && !method.isUndefined();
    }

    public Visibility getVisibility() {
        return method.getVisibility();
    }

    public InternalMethod getMethod() {
        return method;
    }

    public Assumption[] getAssumptions() {
        return assumptions;
    }

}
