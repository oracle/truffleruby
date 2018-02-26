/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.array;

public class DelegatedArrayMirror extends AbstractArrayMirror {

    private final DelegatedArrayStorage storage;
    private final ArrayMirror storageMirror;

    public DelegatedArrayMirror(DelegatedArrayStorage storage, ArrayStrategy typeStrategy) {
        this.storage = storage;
        this.storageMirror = typeStrategy.newMirrorFromStore(storage.storage);
    }

    public DelegatedArrayMirror(DelegatedArrayStorage storage, ArrayMirror storageMirror) {
        this.storage = storage;
        this.storageMirror = storageMirror;
    }

    public int getLength() {
        return storage.length;
    }

    public Object get(int index) {
        return storageMirror.get(index + storage.offset);
    }

    public void set(int index, Object value) {
        throw new UnsupportedOperationException();
    }

    public ArrayMirror copyArrayAndMirror(int newLength) {
        return storageMirror.copyRange(storage.offset, storage.offset + newLength);
    }

    public void copyTo(ArrayMirror destination, int sourceStart, int destinationStart, int count) {
        storageMirror.copyTo(destination, sourceStart + storage.offset, destinationStart, count);
    }

    public void copyTo(Object[] destination, int sourceStart, int destinationStart, int count) {
        storageMirror.copyTo(destination, sourceStart + storage.offset, destinationStart, count);
    }

    public ArrayMirror extractRange(int start, int end) {
        DelegatedArrayStorage newStorage = new DelegatedArrayStorage(storage.storage, storage.offset + start, end - start);
        return new DelegatedArrayMirror(newStorage, storageMirror);
    }

    public ArrayMirror copyRange(int start, int end) {
        if (storage.offset + end <= storageMirror.getLength()) {
            return extractRange(start, end);
        } else {
            return storageMirror.copyRange(storage.offset + start, storage.offset + end);
        }
    }

    public void sort(int size) {
        throw new UnsupportedOperationException();
    }

    public Object getArray() {
        return storage;
    }

    public ArrayMirror copyArrayAndMirror() {
        return new DelegatedArrayMirror(storage, storageMirror);
    }

}
