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

class ObjectArrayMirror extends BasicArrayMirror {

    private final Object[] array;

    public ObjectArrayMirror(Object[] array) {
        this.array = array;
    }

    @Override
    public int getLength() {
        return array.length;
    }

    @Override
    public Object get(int index) {
        return array[index];
    }

    @Override
    public void set(int index, Object value) {
        array[index] = value;
    }

    @Override
    public ArrayMirror newMirror(int newLength) {
        return new ObjectArrayMirror(new Object[newLength]);
    }

    @Override
    public ArrayMirror copyArrayAndMirror() {
        return new ObjectArrayMirror(array.clone());
    }

    @Override
    public ArrayMirror copyArrayAndMirror(int newLength) {
        return new ObjectArrayMirror(ArrayUtils.grow(array, newLength));
    }

    @Override
    public void copyTo(ArrayMirror destination, int sourceStart, int destinationStart, int count) {
        if (destination instanceof ObjectArrayMirror) {
            System.arraycopy(array, sourceStart, destination.getArray(), destinationStart, count);
        } else {
            for (int i = 0; i < count; i++) {
                destination.set(destinationStart + i, array[sourceStart + i]);
            }
        }
    }

    @Override
    public void copyTo(Object[] destination, int sourceStart, int destinationStart, int count) {
        System.arraycopy(array, sourceStart, destination, destinationStart, count);
    }

    @Override
    public void sort(int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getArray() {
        return array;
    }

}
