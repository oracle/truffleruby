/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.collections;

public class Memo<T> {

    private T value;

    public Memo(T initial) {
        value = initial;
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }


}
