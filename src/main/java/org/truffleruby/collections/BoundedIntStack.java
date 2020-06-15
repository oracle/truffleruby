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

public final class BoundedIntStack {

    final int[] storage;
    int index = -1;

    public BoundedIntStack(int length) {
        this.storage = new int[length];
    }

    public boolean isEmpty() {
        return index == -1;
    }

    public void push(int value) {
        storage[++index] = value;
    }

    public int peek() {
        return storage[index];
    }

    public int pop() {
        return storage[index--];
    }

}
