/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

/** An append-only map of names or identifiers to indices. The map is stored in {@link org.truffleruby.RubyLanguage} and
 * each {@link org.truffleruby.RubyContext} have separate {@link ContextArray}s. This enables looking up names and
 * storing only a constant index in the AST and yet handle multiple contexts sharing that AST and having different
 * values for a name. */
public final class SharedIndicesMap {

    private final AtomicInteger nextIndex = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Integer> nameToIndex = new ConcurrentHashMap<>();

    /** Find the index for an existing name, or allocate a new index for this name */
    @TruffleBoundary
    public int lookup(String name) {
        // We need the semantics of ConcurrentHashMap#computeIfAbsent() here to ensure the lambda is only executed once per missing key.
        // Otherwise we could waste unused indices.
        return ConcurrentOperations.getOrCompute(nameToIndex, name, k -> nextIndex.getAndIncrement());
    }

    public int size() {
        return nextIndex.get();
    }

    private static class LazyDataArray<T> {

        protected final SharedIndicesMap sharedIndicesMap;
        protected final Supplier<T> createWhenAbsent;
        /** Reading from this field in PE code must be very careful to only return if the value is non-null. Otherwise
         * it might miss updates if e.g. the array was reassigned by some other access. */
        @CompilationFinal(dimensions = 1) protected T[] data;

        protected LazyDataArray(
                SharedIndicesMap sharedIndicesMap,
                IntFunction<T[]> newArray,
                Supplier<T> createWhenAbsent) {
            this.sharedIndicesMap = sharedIndicesMap;
            this.createWhenAbsent = createWhenAbsent;
            this.data = newArray.apply(sharedIndicesMap.size());
        }

        public T get(int index) {
            CompilerAsserts.partialEvaluationConstant(index);
            assert index <= sharedIndicesMap.size();

            final T[] localData = this.data;
            if (index < localData.length) {
                T value = localData[index];
                if (value != null) {
                    // We can only return if the value is non-null, see the comment on the field
                    return value;
                }
            }

            if (CompilerDirectives.isPartialEvaluationConstant(data)) {
                // To see the new data next time
                CompilerDirectives.transferToInterpreterAndInvalidate();
            }
            return getSlowPath(index);
        }

        @TruffleBoundary
        protected T getSlowPath(int index) {
            synchronized (this) {
                growIfNeeded(index);

                final T prev = data[index];
                if (prev == null) {
                    // Must always be synchronized to prevent other accesses to grow the array concurrently
                    T value = createWhenAbsent.get();
                    assert value != null;
                    data[index] = value;
                    return value;
                } else {
                    return prev;
                }
            }
        }

        @TruffleBoundary
        public boolean contains(int index) {
            assert index <= sharedIndicesMap.size();

            final T[] localData = this.data;
            return index < localData.length && localData[index] != null;
        }

        @TruffleBoundary
        public Collection<String> keys() {
            List<String> keys = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : sharedIndicesMap.nameToIndex.entrySet()) {
                int index = entry.getValue();
                if (contains(index)) {
                    keys.add(entry.getKey());
                }
            }
            return keys;
        }

        @TruffleBoundary
        public Collection<T> values() {
            List<T> values = new ArrayList<>();
            for (T element : data) {
                if (element != null) {
                    values.add(element);
                }
            }
            return values;
        }

        @TruffleBoundary
        protected void growIfNeeded(int index) {
            if (index >= data.length) {
                grow();
            }
        }

        @TruffleBoundary
        private void grow() {
            int newSize = Math.max(sharedIndicesMap.size(), data.length << 1);
            this.data = Arrays.copyOf(data, newSize);
        }

    }

    /** An array holding context-specific values. A value at a given index can not be set to null again. Reads fold to a
     * constant in single-context mode, if the index is already set. */
    public static final class ContextArray<T> extends LazyDataArray<T> {

        public ContextArray(
                SharedIndicesMap sharedIndicesMap,
                IntFunction<T[]> newArray,
                Supplier<T> createWhenAbsent) {
            super(sharedIndicesMap, newArray, createWhenAbsent);
        }

        /** Returns {@code value} if the index was unset, and the previous value otherwise */
        @TruffleBoundary
        public T addIfAbsent(int index, T value) {
            assert value != null;
            assert index <= sharedIndicesMap.size();

            synchronized (this) {
                growIfNeeded(index);

                final T prev = data[index];
                if (prev == null) {
                    // Must always be synchronized to prevent other accesses to grow the array concurrently
                    data[index] = value;
                    return value;
                } else {
                    return prev;
                }
            }
        }

        /** Returns the previous value or null if the index was unset. Using this method requires external invalidation
         * (e.g., via an Assumption) if the index was already set, to be noticed from get() which reads from a
         * CompilationFinal array */
        @TruffleBoundary
        public T set(int index, T value) {
            assert value != null;
            assert index <= sharedIndicesMap.size();

            synchronized (this) {
                growIfNeeded(index);

                final T prev = data[index];
                // Must always be synchronized to prevent other accesses to grow the array concurrently
                data[index] = value;
                return prev;
            }
        }

        /** Sees updated values from {@link #set(int, Object)} even without external invalidation */
        @TruffleBoundary
        public T getNonConstant(int index) {
            assert index <= sharedIndicesMap.size();

            final T[] localData = this.data;
            if (index < localData.length) {
                T value = localData[index];
                if (value != null) {
                    return value;
                }
            }

            return getSlowPath(index);
        }

    }

    /** An array holding language/context-agnostic values like Assumptions. A value at a given index can not be set to
     * null again. */
    public static final class LanguageArray<T> extends LazyDataArray<T> {

        public LanguageArray(
                SharedIndicesMap sharedIndicesMap,
                IntFunction<T[]> newArray,
                Supplier<T> createWhenAbsent) {
            super(sharedIndicesMap, newArray, createWhenAbsent);
        }

        @TruffleBoundary
        @Override
        public T get(int index) {
            return super.get(index);
        }
    }

}
