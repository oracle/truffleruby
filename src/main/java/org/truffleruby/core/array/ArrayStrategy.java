/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCapacityNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayGetNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArraySetNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayNewStoreNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayBoxedCopyNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCommonUnshareStorageNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCopyStoreNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCopyToNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayExtractRangeNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArraySortNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayUnshareStorageNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class ArrayStrategy {

    // ArrayStrategy interface

    protected Class<?> type() {
        throw unsupported();
    }

    public boolean canStore(Class<?> type) {
        throw unsupported();
    }

    /**
     * When the strategy {@link #matches(DynamicObject)} an Array, this can be used to see if the
     * given value can be written in the Array without generalizing the storage.
     */
    public abstract boolean accepts(Object value);

    public abstract boolean isPrimitive();

    public abstract boolean isStorageMutable();

    public abstract ArrayGetNode getNode();

    public abstract ArraySetNode setNode();

    public abstract ArrayNewStoreNode newStoreNode();

    public abstract ArrayCopyStoreNode copyStoreNode();

    public abstract ArrayCopyToNode copyToNode();

    public abstract ArraySortNode sortNode();

    public abstract ArrayCapacityNode capacityNode();

    public abstract ArrayExtractRangeNode extractRangeNode();

    public ArrayBoxedCopyNode boxedCopyNode() {
        return ArrayBoxedCopyNode.create(this);
    }

    public ArrayUnshareStorageNode unshareNode() {
        return ArrayCommonUnshareStorageNode.create();
    }

    public Iterable<Object> getIterable(DynamicObject array, int length) {
        return getIterableFrom(Layouts.ARRAY.getStore(array), 0, length);
    }

    protected abstract Iterable<Object> getIterableFrom(Object array, int from, int length);

    /**
     * Whether the strategy obtained from {@link #forValue(Object)} describes accurately the kind of
     * array storage needed to store this value (so e.g., Object[] specializesFor non-int/long/double).
     */
    public boolean specializesFor(Object value) {
        throw unsupported();
    }

    public boolean isDefaultValue(Object value) {
        throw unsupported();
    }

    /** Whether {@code this} is the strategy of {@code array}. */
    public final boolean matches(DynamicObject array) {
        return matchesStore(Layouts.ARRAY.getStore(array));
    }

    public abstract boolean matchesStore(Object store);

    public int getSize(DynamicObject array) {
        return Layouts.ARRAY.getSize(array);
    }

    public abstract ArrayStrategy sharedStorageStrategy();

    public Object makeStorageShared(DynamicObject array) {
        final Object currentMirror = Layouts.ARRAY.getStore(array);
        DelegatedArrayStorage newStore = new DelegatedArrayStorage(currentMirror, 0, getSize(array));
        setStore(array, newStore);
        return newStore;
    }

    public void setStore(DynamicObject array, Object store) {
        Layouts.ARRAY.setStore(array, store);
    }

    public void setStoreAndSize(DynamicObject array, Object store, int size) {
        setStore(array, store);
        ArrayHelpers.setSize(array, size);
    }

    @Override
    public abstract String toString();

    @TruffleBoundary
    public ArrayStrategy generalize(ArrayStrategy other) {
        if (other == this) {
            return this;
        }

        if (other instanceof NullArrayStrategy) {
            return this.generalizeForMutation();
        } else if (this instanceof NullArrayStrategy) {
            return other.generalizeForMutation();
        }

        for (ArrayStrategy generalized : TYPE_STRATEGIES) {
            if (generalized.canStore(type()) && generalized.canStore(other.type())) {
                return generalized;
            }
        }
        throw unsupported();
    }

    public ArrayStrategy generalizeForMutation() {
        return this;
    }

    // Helpers

    protected RuntimeException unsupported() {
        return new UnsupportedOperationException(toString());
    }

    public static final ArrayStrategy[] TYPE_STRATEGIES = {
            IntArrayStrategy.INSTANCE,
            LongArrayStrategy.INSTANCE,
            DoubleArrayStrategy.INSTANCE,
            ObjectArrayStrategy.INSTANCE
    };

    @TruffleBoundary
    public static ArrayStrategy ofStore(Object store) {
        if (store == null) {
            return NullArrayStrategy.INSTANCE;
        } else if (store instanceof int[]) {
            return IntArrayStrategy.INSTANCE;
        } else if (store instanceof long[]) {
            return LongArrayStrategy.INSTANCE;
        } else if (store instanceof double[]) {
            return DoubleArrayStrategy.INSTANCE;
        } else if (store.getClass() == Object[].class) {
            return ObjectArrayStrategy.INSTANCE;
        } else if (store instanceof DelegatedArrayStorage) {
            return ofDelegatedStore((DelegatedArrayStorage) store);
        } else {
            throw new UnsupportedOperationException(store.getClass().getName());
        }
    }

    private static ArrayStrategy ofDelegatedStore(DelegatedArrayStorage delegatedArrayStorage) {
        Object store = delegatedArrayStorage.storage;
        if (store == null) {
            return NullArrayStrategy.DELEGATED_INSTANCE;
        } else if (store instanceof int[]) {
            return IntArrayStrategy.DELEGATED_INSTANCE;
        } else if (store instanceof long[]) {
            return LongArrayStrategy.DELEGATED_INSTANCE;
        } else if (store instanceof double[]) {
            return DoubleArrayStrategy.DELEGATED_INSTANCE;
        } else if (store.getClass() == Object[].class) {
            return ObjectArrayStrategy.DELEGATED_INSTANCE;
        } else {
            throw new UnsupportedOperationException(store.getClass().getName());
        }
    }

    @TruffleBoundary
    public static ArrayStrategy of(DynamicObject array) {
        if (!RubyGuards.isRubyArray(array)) {
            return FallbackArrayStrategy.INSTANCE;
        }

        return ofStore(Layouts.ARRAY.getStore(array));
    }

    /**
     * Use together with {@link #specializesFor(Object)}, not {@link #accepts(Object)}.
     */
    @TruffleBoundary
    public static ArrayStrategy forValue(Object value) {
        if (value instanceof Integer) {
            return IntArrayStrategy.INSTANCE;
        } else if (value instanceof Long) {
            return LongArrayStrategy.INSTANCE;
        } else if (value instanceof Double) {
            return DoubleArrayStrategy.INSTANCE;
        } else {
            return ObjectArrayStrategy.INSTANCE;
        }
    }

    // Type strategies (int, long, double, Object)

    private static class IntArrayStrategy extends ArrayStrategy {

        static final ArrayStrategy INSTANCE = new IntArrayStrategy();
        static final ArrayStrategy DELEGATED_INSTANCE = new DelegatedArrayStrategy(INSTANCE);

        @Override
        public Class<?> type() {
            return Integer.class;
        }

        @Override
        public boolean canStore(Class<?> type) {
            return type == Integer.class;
        }

        @Override
        public boolean accepts(Object value) {
            return value instanceof Integer;
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }

        @Override
        public boolean isStorageMutable() {
            return true;
        }

        @Override
        public boolean specializesFor(Object value) {
            return value instanceof Integer;
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return (int) value == 0;
        }

        @Override
        public boolean matchesStore(Object store) {
            return store instanceof int[];
        }

        @Override
        public String toString() {
            return "int[]";
        }

        @Override
        public ArrayCapacityNode capacityNode() {
            return IntegerArrayNodes.IntArrayCapacityNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return IntegerArrayNodes.IntArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return IntegerArrayNodes.IntArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return IntegerArrayNodes.IntArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return IntegerArrayNodes.IntArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return IntegerArrayNodes.IntArrayCopyToNode.create();
        }

        @Override
        public ArrayExtractRangeNode extractRangeNode() {
            return IntegerArrayNodes.IntArrayExtractRangeNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return IntegerArrayNodes.ArraySortNode.create();
        }

        @Override
        public ArrayStrategy sharedStorageStrategy() {
            return DELEGATED_INSTANCE;
        }

        @Override
        protected Iterable<Object> getIterableFrom(Object array, int from, int length) {
            int[] store = (int[]) array;
            return () -> new Iterator<Object>() {

                private int n = from;

                @Override
                public boolean hasNext() {
                    return n < from + length;
                }

                @Override
                public Object next() throws NoSuchElementException {
                    if (n >= from + length) {
                        throw new NoSuchElementException();
                    }

                    final Object object = store[n];
                    n++;
                    return object;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }

            };
        }
    }

    private static class LongArrayStrategy extends ArrayStrategy {

        static final ArrayStrategy INSTANCE = new LongArrayStrategy();
        static final ArrayStrategy DELEGATED_INSTANCE = new DelegatedArrayStrategy(INSTANCE);

        @Override
        public Class<?> type() {
            return Long.class;
        }

        @Override
        public boolean canStore(Class<?> type) {
            return type == Long.class || type == Integer.class;
        }

        @Override
        public boolean accepts(Object value) {
            return value instanceof Long;
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }

        @Override
        public boolean isStorageMutable() {
            return true;
        }

        @Override
        public boolean specializesFor(Object value) {
            return value instanceof Long;
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return (long) value == 0L;
        }

        @Override
        public boolean matchesStore(Object store) {
            return store instanceof long[];
        }

        @Override
        public String toString() {
            return "long[]";
        }

        @Override
        public ArrayCapacityNode capacityNode() {
            return LongArrayNodes.LongArrayCapacityNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return LongArrayNodes.LongArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return LongArrayNodes.LongArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return LongArrayNodes.LongArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return LongArrayNodes.LongArrayCopyStoreNode.create();
        }

        @Override
        public ArrayExtractRangeNode extractRangeNode() {
            return LongArrayNodes.LongArrayExtractRangeNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return LongArrayNodes.LongArrayCopyToNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return LongArrayNodes.LongArraySortNode.create();
        }

        @Override
        public ArrayStrategy sharedStorageStrategy() {
            return DELEGATED_INSTANCE;
        }

        @Override
        protected Iterable<Object> getIterableFrom(Object array, int from, int length) {
            long[] store = (long[]) array;
            return () -> new Iterator<Object>() {

                private int n = from;

                @Override
                public boolean hasNext() {
                    return n < from + length;
                }

                @Override
                public Object next() throws NoSuchElementException {
                    if (n >= from + length) {
                        throw new NoSuchElementException();
                    }

                    final Object object = store[n];
                    n++;
                    return object;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }

            };
        }
    }

    private static class DoubleArrayStrategy extends ArrayStrategy {

        static final ArrayStrategy INSTANCE = new DoubleArrayStrategy();
        static final ArrayStrategy DELEGATED_INSTANCE = new DelegatedArrayStrategy(INSTANCE);

        @Override
        public Class<?> type() {
            return Double.class;
        }

        @Override
        public boolean canStore(Class<?> type) {
            return type == Double.class;
        }

        @Override
        public boolean accepts(Object value) {
            return value instanceof Double;
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }

        @Override
        public boolean isStorageMutable() {
            return true;
        }

        @Override
        public boolean specializesFor(Object value) {
            return value instanceof Double;
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return (double) value == 0.0;
        }

        @Override
        public boolean matchesStore(Object store) {
            return store instanceof double[];
        }

        @Override
        public String toString() {
            return "double[]";
        }

        @Override
        public ArrayCapacityNode capacityNode() {
            return DoubleArrayNodes.DoubleArrayCapacityNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return DoubleArrayNodes.DoubleArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return DoubleArrayNodes.DoubleArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return DoubleArrayNodes.DoubleArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return DoubleArrayNodes.DoubleArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return DoubleArrayNodes.DoubleArrayCopyToNode.create();
        }

        @Override
        public ArrayExtractRangeNode extractRangeNode() {
            return DoubleArrayNodes.DoubleArrayExtractRangeNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return DoubleArrayNodes.DoubleArraySortNode.create();
        }

        @Override
        public ArrayStrategy sharedStorageStrategy() {
            return DELEGATED_INSTANCE;
        }

        @Override
        protected Iterable<Object> getIterableFrom(Object array, int from, int length) {
            double[] store = (double[]) array;
            return () -> new Iterator<Object>() {

                private int n = from;

                @Override
                public boolean hasNext() {
                    return n < from + length;
                }

                @Override
                public Object next() throws NoSuchElementException {
                    if (n >= from + length) {
                        throw new NoSuchElementException();
                    }

                    final Object object = store[n];
                    n++;
                    return object;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }

            };
        }
    }

    private static class ObjectArrayStrategy extends ArrayStrategy {

        static final ArrayStrategy INSTANCE = new ObjectArrayStrategy();
        static final ArrayStrategy DELEGATED_INSTANCE = new DelegatedArrayStrategy(INSTANCE);

        @Override
        public Class<?> type() {
            return Object.class;
        }

        @Override
        public boolean canStore(Class<?> type) {
            return true;
        }

        @Override
        public boolean accepts(Object value) {
            return true;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isStorageMutable() {
            return true;
        }

        @Override
        public boolean specializesFor(Object value) {
            return !(value instanceof Integer) && !(value instanceof Long) && !(value instanceof Double);
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return value == null;
        }

        @Override
        public boolean matchesStore(Object store) {
            return store != null && store.getClass() == Object[].class;
        }

        @Override
        public String toString() {
            return "Object[]";
        }

        @Override
        public ArrayCapacityNode capacityNode() {
            return ObjectArrayNodes.ObjectArrayCapacityNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return ObjectArrayNodes.ObjectArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return ObjectArrayNodes.ObjectArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return ObjectArrayNodes.ObjectArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return ObjectArrayNodes.ObjectArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return ObjectArrayNodes.ObjectArrayCopyToNode.create();
        }

        @Override
        public ArrayExtractRangeNode extractRangeNode() {
            return ObjectArrayNodes.ObjectArrayExtractRangeNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return ObjectArrayNodes.ObjectArraySortNode.create();
        }

        @Override
        public ArrayStrategy sharedStorageStrategy() {
            return DELEGATED_INSTANCE;
        }

        @Override
        protected Iterable<Object> getIterableFrom(Object array, int from, int length) {
            Object[] store = (Object[]) array;
            return () -> new Iterator<Object>() {

                private int n = from;

                @Override
                public boolean hasNext() {
                    return n < from + length;
                }

                @Override
                public Object next() throws NoSuchElementException {
                    if (n >= from + length) {
                        throw new NoSuchElementException();
                    }

                    final Object object = store[n];
                    n++;
                    return object;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }

            };
        }
    }

    // Null/empty strategy

    private static class NullArrayStrategy extends ArrayStrategy {

        static final ArrayStrategy INSTANCE = new NullArrayStrategy();
        static final ArrayStrategy DELEGATED_INSTANCE = new DelegatedArrayStrategy(INSTANCE);

        @Override
        public Class<?> type() {
            throw unsupported();
        }

        @Override
        public boolean canStore(Class<?> type) {
            throw unsupported();
        }

        @Override
        public boolean accepts(Object value) {
            return false; // Cannot write any element in a null storage
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isStorageMutable() {
            return false;
        }

        @Override
        public ArrayStrategy generalizeForMutation() {
            return IntArrayStrategy.INSTANCE;
        }

        @Override
        public Object makeStorageShared(DynamicObject array) {
            return null;
        }

        @Override
        public boolean matchesStore(Object store) {
            return store == null;
        }

        @Override
        public int getSize(DynamicObject array) {
            return 0;
        }

        @Override
        public String toString() {
            return "null";
        }

        @Override
        public ArrayCapacityNode capacityNode() {
            return EmptyArrayNodes.EmptyArrayCapacityNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return EmptyArrayNodes.EmptyArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return EmptyArrayNodes.EmptyArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return EmptyArrayNodes.EmptyArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return EmptyArrayNodes.EmptyArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return EmptyArrayNodes.EmptyArrayCopyToNode.create();
        }

        @Override
        public ArrayExtractRangeNode extractRangeNode() {
            return EmptyArrayNodes.EmptyArrayExtractRangeNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return EmptyArrayNodes.EmptyArraySortNode.create();
        }

        @Override
        public ArrayStrategy sharedStorageStrategy() {
            return this;
        }

        @Override
        public Iterable<Object> getIterableFrom(Object array, int from, int length) {
            return () -> new Iterator<Object>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Object next() throws NoSuchElementException {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }

            };
        }
    }

    private static class DelegatedArrayStrategy extends ArrayStrategy {

        private final ArrayStrategy typeStrategy;

        @Override
        protected Class<?> type() {
            return typeStrategy.type();
        }

        public DelegatedArrayStrategy(ArrayStrategy typeStrategy) {
            this.typeStrategy = typeStrategy;
        }

        @Override
        public boolean accepts(Object value) {
            return false; // Cannot write to a DelegatedArrayStrategy, need to unshare the storage
        }

        @Override
        public boolean isPrimitive() {
            return typeStrategy.isPrimitive();
        }

        @Override
        public boolean isStorageMutable() {
            return false;
        }

        @Override
        public ArrayStrategy generalize(ArrayStrategy other) {
            return typeStrategy.generalize(other);
        }

        @Override
        public ArrayStrategy generalizeForMutation() {
            return typeStrategy;
        }

        @Override
        public boolean matchesStore(Object store) {
            return store instanceof DelegatedArrayStorage && typeStrategy.matchesStore(((DelegatedArrayStorage) store).storage);
        }

        @Override
        public Object makeStorageShared(DynamicObject array) {
            return Layouts.ARRAY.getStore(array);
        }

        @TruffleBoundary
        @Override
        public String toString() {
            return String.format("Delegate of (%s)", typeStrategy);
        }

        @Override
        public ArrayGetNode getNode() {
            return DelegateArrayNodes.DelegateArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            throw unsupported();
        }

        @Override
        public ArrayCapacityNode capacityNode() {
            return DelegateArrayNodes.DelegateArrayCapacityNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return DelegateArrayNodes.DelegateArrayNewStoreNode.create(typeStrategy);
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return DelegateArrayNodes.DelegateArrayCopyStoreNode.create(typeStrategy);
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return DelegateArrayNodes.DelegateArrayCopyToNode.create(typeStrategy);
        }

        @Override
        public ArrayExtractRangeNode extractRangeNode() {
            return DelegateArrayNodes.DelegateArrayExtractRangeNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            throw unsupported();
        }

        @Override
        public ArrayStrategy sharedStorageStrategy() {
            return this;
        }

        @Override
        public ArrayUnshareStorageNode unshareNode() {
            return DelegateArrayNodes.DelegateArrayUnshareStoreNode.create(typeStrategy);
        }

        @Override
        public Iterable<Object> getIterableFrom(Object array, int from, int length) {
            DelegatedArrayStorage store = (DelegatedArrayStorage) array;
            return typeStrategy.getIterableFrom(store.storage, from + store.offset, length);
        }
    }

    // Fallback strategy

    private static class FallbackArrayStrategy extends ArrayStrategy {

        static final ArrayStrategy INSTANCE = new FallbackArrayStrategy();

        @Override
        public boolean accepts(Object value) {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isStorageMutable() {
            return false;
        }

        @Override
        public boolean matchesStore(Object store) {
            return false;
        }

        @Override
        public ArrayStrategy generalize(ArrayStrategy other) {
            return other;
        }

        @Override
        public String toString() {
            return "fallback";
        }

        @Override
        public ArrayCapacityNode capacityNode() {
            throw unsupported();
        }

        @Override
        public ArrayGetNode getNode() {
            throw unsupported();
        }

        @Override
        public ArraySetNode setNode() {
            throw unsupported();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            throw unsupported();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            throw unsupported();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            throw unsupported();
        }

        @Override
        public ArrayExtractRangeNode extractRangeNode() {
            throw unsupported();
        }

        @Override
        public ArraySortNode sortNode() {
            throw unsupported();
        }

        @Override
        public ArrayStrategy sharedStorageStrategy() {
            throw unsupported();
        }

        @Override
        public Iterable<Object> getIterableFrom(Object array, int from, int length) {
            throw unsupported();
        }
    }
}
