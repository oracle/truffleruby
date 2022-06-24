/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.collections;

import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<T> {

    private volatile T value = null;
    private Supplier<T> supplier;

    public CachedSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (isAvailable()) {
            return value;
        }
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
