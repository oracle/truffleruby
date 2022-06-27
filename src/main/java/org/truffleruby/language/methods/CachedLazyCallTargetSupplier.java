/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;

import java.util.function.Supplier;

public class CachedLazyCallTargetSupplier {

    private volatile RootCallTarget value = null;
    private Supplier<RootCallTarget> supplier;

    public CachedLazyCallTargetSupplier(Supplier<RootCallTarget> supplier) {
        this.supplier = supplier;
    }

    public RootCallTarget get() {
        if (isAvailable()) {
            return value;
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        synchronized (this) {
            if (value == null) {
                value = supplier.get();
                supplier = null;
            }
            return value;
        }
    }

    public boolean isAvailable() {
        return value != null;
    }

}
