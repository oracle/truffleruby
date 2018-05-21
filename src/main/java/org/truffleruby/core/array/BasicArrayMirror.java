/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;



public abstract class BasicArrayMirror extends AbstractArrayMirror {

    @Override
    public ArrayMirror extractRange(int start, int end) {
        ArrayMirror newMirror = newMirror(end - start);
        copyTo(newMirror, start, 0, end - start);
        return newMirror;
    }

}
