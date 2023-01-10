/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

/** A concurrent thread-safe set with weak elements */
public final class ConcurrentWeakSet<E> extends ConcurrentWeakKeysMap<E, Boolean> implements Iterable<E> {

    public ConcurrentWeakSet() {
    }

    @TruffleBoundary
    public boolean add(E element) {
        return put(element, Boolean.TRUE) == null;
    }

    @TruffleBoundary
    public Object[] toArray() {
        return keys().toArray();
    }

    @TruffleBoundary
    public WeakSetIterator<E> iterator() {
        return new WeakSetIterator<E>(map.keySet().iterator());
    }

    private static final class WeakSetIterator<E> implements Iterator<E> {
        private final Iterator<WeakKeyReference<E>> keysIterator;
        private E nextElement;

        private WeakSetIterator(Iterator<WeakKeyReference<E>> keysIterator) {
            this.keysIterator = keysIterator;
            computeNext();
        }

        private void computeNext() {
            // hasNext()+next() is safe because the ConcurrentHashMap keySet iterator saves the next value
            while (keysIterator.hasNext()) {
                E element = keysIterator.next().get();
                if (element != null) {
                    this.nextElement = element;
                    return;
                }
            }
            this.nextElement = null;
        }

        @Override
        public boolean hasNext() {
            return nextElement != null;
        }

        @TruffleBoundary
        @Override
        public E next() {
            final E element = nextElement;
            if (element == null) {
                throw new NoSuchElementException();
            }
            computeNext();
            return element;
        }
    }

}
