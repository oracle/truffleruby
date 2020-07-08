/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import com.oracle.truffle.api.object.DynamicObject;

public class SuperMethodLookup {
    private final MethodLookupResult result;
    private final boolean foundDeclaringModule;
    private final DynamicObject lookupTo;

    public SuperMethodLookup(MethodLookupResult result, boolean foundDeclaringModule, DynamicObject lookupTo) {
        this.result = result;
        this.foundDeclaringModule = foundDeclaringModule;
        this.lookupTo = lookupTo;
    }

    public SuperMethodLookup withResult(MethodLookupResult result) {
        if (result == null) {
            return this;
        } else {
            return new SuperMethodLookup(result, foundDeclaringModule, lookupTo);
        }
    }

    public SuperMethodLookup withFoundDeclaringModule(boolean foundDeclaringModule) {
        if (foundDeclaringModule == this.foundDeclaringModule) {
            return this;
        } else {
            return new SuperMethodLookup(result, foundDeclaringModule, lookupTo);
        }
    }

    public SuperMethodLookup withLookupTo(DynamicObject lookupTo) {
        return new SuperMethodLookup(result, foundDeclaringModule, lookupTo);
    }

    public MethodLookupResult getResult() {
        return this.result;
    }

    public boolean getFoundDeclaringModule() {
        return this.foundDeclaringModule;
    }

    public DynamicObject getLookupTo() {
        return this.lookupTo;
    }
}
