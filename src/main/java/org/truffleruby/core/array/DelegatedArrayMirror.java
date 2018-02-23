package org.truffleruby.core.array;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DelegatedArrayMirror implements ArrayMirror {

    private DelegatedArrayStorage storage;
    private ArrayMirror storageMirror;

    public DelegatedArrayMirror(DelegatedArrayStorage storage, ArrayStrategy typeStrategy) {
        this.storage = storage;
        storageMirror = typeStrategy.newMirrorFromStore(storage.storage);
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

    public Object[] getBoxedCopy() {
        Object[] destination = new Object[storage.length];
        copyTo(destination, 0, 0, storage.length);
        return destination;
    }

    public Object[] getBoxedCopy(int newLength) {
        Object[] destination = new Object[newLength];
        copyTo(destination, 0, 0, Math.min(newLength, storage.length));
        return destination;
    }

    public Iterable<Object> iterableUntil(int length) {
        return new Iterable<Object>() {

            private int n = 0;

            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {

                    @Override
                    public boolean hasNext() {
                        return n < length;
                    }

                    @Override
                    public Object next() throws NoSuchElementException {
                        if (n >= length) {
                            throw new NoSuchElementException();
                        }

                        final Object object = get(n);
                        n++;
                        return object;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }

                };
            }
        };
    }

}
