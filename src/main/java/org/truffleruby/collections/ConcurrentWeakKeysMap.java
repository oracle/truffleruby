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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** A concurrent thread-safe map with weak keys. Keys cannot be null. This map currently assumes keys do no depend on
 * Hashing and so there is no need for ReHashable. So far this is only used for keys which are compared by identity. */
public class ConcurrentWeakKeysMap<Key, Value> {

    protected final ConcurrentHashMap<WeakKeyReference<Key>, Value> map = new ConcurrentHashMap<>();
    private final ReferenceQueue<Key> referenceQueue = new ReferenceQueue<>();

    public ConcurrentWeakKeysMap() {
    }

    @TruffleBoundary
    public Value get(Key key) {
        removeStaleEntries();
        return map.get(new WeakKeyReference<>(key));
    }

    public boolean contains(Key key) {
        return get(key) != null;
    }

    /** Sets the value in the cache, always returns the old value. */
    @TruffleBoundary
    public Value put(Key key, Value value) {
        removeStaleEntries();
        var ref = new WeakKeyReference<>(key, referenceQueue);
        return map.put(ref, value);
    }

    @TruffleBoundary
    public Collection<Key> keys() {
        removeStaleEntries();
        final Collection<Key> keys = new ArrayList<>(map.size());

        // Filter out keys for null values.
        for (var e : map.keySet()) {
            final Key key = e.get();
            if (key != null) {
                keys.add(key);
            }
        }

        return keys;
    }

    /** Attempts to remove map entries whose values have been made unreachable by the GC.
     * <p>
     * This relies on the underlying {@link WeakReference} instance being enqueued to the {@link #referenceQueue} queue.
     * It is possible that the map still contains {@link WeakReference} instances whose key has been nulled out after a
     * call to this method (the reference not having been enqueued yet)! */
    private void removeStaleEntries() {
        WeakKeyReference<?> ref;
        while ((ref = (WeakKeyReference<?>) referenceQueue.poll()) != null) {
            // Here ref.get() is null, so it will not remove a new key-value pair with the same key
            // as that is a different WeakKeyReference instance.
            map.remove(ref);
        }
    }

    protected static final class WeakKeyReference<Key> extends WeakReference<Key> {
        private final int hashCode;

        public WeakKeyReference(Key key) {
            super(key);
            Objects.requireNonNull(key);
            this.hashCode = key.hashCode();
        }

        public WeakKeyReference(Key key, ReferenceQueue<? super Key> queue) {
            super(key, queue);
            Objects.requireNonNull(key);
            this.hashCode = key.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof WeakKeyReference) {
                WeakKeyReference<?> ref = (WeakKeyReference<?>) other;
                Key key = get();
                Object otherKey = ref.get();
                return key != null && otherKey != null && key.equals(otherKey);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

}
