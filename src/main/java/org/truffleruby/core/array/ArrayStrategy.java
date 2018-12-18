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

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayLengthNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayGetNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArraySetNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayNewStoreNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCopyStoreNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCopyToNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArraySortNode;
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

    public abstract ArrayLengthNode lengthNode();
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

    public ArrayMirror makeStorageUnshared(DynamicObject array) {
        return newMirror(array);
    }

    public ArrayMirror makeStorageShared(DynamicObject array) {
        final ArrayMirror currentMirror = newMirror(array);
        DelegatedArrayStorage newStore = new DelegatedArrayStorage(currentMirror.getArray(), 0, getSize(array));
        setStore(array, newStore);
        return new DelegatedArrayMirror(newStore, this);
    }

    public abstract ArrayMirror newArray(int size);

    public final ArrayMirror newMirror(DynamicObject array) {
        return newMirrorFromStore(Layouts.ARRAY.getStore(array));
    }

    public ArrayMirror newMirrorFromStore(Object store) {
        throw unsupported();
    }

    public void setStore(DynamicObject array, Object store) {
        assert !(store instanceof ArrayMirror);
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
        public ArrayMirror newArray(int size) {
            return new IntegerArrayMirror(new int[size]);
        }

        @Override
        public ArrayMirror newMirrorFromStore(Object store) {
            return new IntegerArrayMirror((int[]) store);
        }

        @Override
        public String toString() {
            return "int[]";
        }

        @Override
        public ArrayLengthNode lengthNode() {
            return IntegerArrayNodes.ArrayLengthNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return IntegerArrayNodes.ArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return IntegerArrayNodes.ArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return IntegerArrayNodes.ArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return IntegerArrayNodes.ArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return IntegerArrayNodes.ArrayCopyToNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return IntegerArrayNodes.ArraySortNode.create();
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
        public ArrayMirror newArray(int size) {
            return new LongArrayMirror(new long[size]);
        }

        @Override
        public ArrayMirror newMirrorFromStore(Object store) {
            return new LongArrayMirror((long[]) store);
        }

        @Override
        public String toString() {
            return "long[]";
        }

        @Override
        public ArrayLengthNode lengthNode() {
            return LongArrayNodes.ArrayLengthNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return LongArrayNodes.ArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return LongArrayNodes.ArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return LongArrayNodes.ArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return LongArrayNodes.ArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return LongArrayNodes.ArrayCopyToNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return LongArrayNodes.ArraySortNode.create();
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
        public ArrayMirror newArray(int size) {
            return new DoubleArrayMirror(new double[size]);
        }

        @Override
        public ArrayMirror newMirrorFromStore(Object store) {
            return new DoubleArrayMirror((double[]) store);
        }

        @Override
        public String toString() {
            return "double[]";
        }

        @Override
        public ArrayLengthNode lengthNode() {
            return DoubleArrayNodes.ArrayLengthNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return DoubleArrayNodes.ArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return DoubleArrayNodes.ArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return DoubleArrayNodes.ArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return DoubleArrayNodes.ArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return DoubleArrayNodes.ArrayCopyToNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return DoubleArrayNodes.ArraySortNode.create();
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
        public ArrayMirror newArray(int size) {
            return new ObjectArrayMirror(new Object[size]);
        }

        @Override
        public ArrayMirror newMirrorFromStore(Object store) {
            return new ObjectArrayMirror((Object[]) store);
        }

        @Override
        public String toString() {
            return "Object[]";
        }

        @Override
        public ArrayLengthNode lengthNode() {
            return ObjectArrayNodes.ArrayLengthNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return ObjectArrayNodes.ArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return ObjectArrayNodes.ArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return ObjectArrayNodes.ArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return ObjectArrayNodes.ArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return ObjectArrayNodes.ArrayCopyToNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return ObjectArrayNodes.ArraySortNode.create();
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
        public ArrayMirror makeStorageShared(DynamicObject array) {
            return EmptyArrayMirror.INSTANCE;
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
        public ArrayMirror newArray(int size) {
            assert size == 0;
            return EmptyArrayMirror.INSTANCE;
        }

        @Override
        public ArrayMirror newMirrorFromStore(Object store) {
            return EmptyArrayMirror.INSTANCE;
        }

        @Override
        public String toString() {
            return "null";
        }

        @Override
        public ArrayLengthNode lengthNode() {
            return EmptyArrayNodes.ArrayLengthNode.create();
        }

        @Override
        public ArrayGetNode getNode() {
            return EmptyArrayNodes.ArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            return EmptyArrayNodes.ArraySetNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return EmptyArrayNodes.ArrayNewStoreNode.create();
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return EmptyArrayNodes.ArrayCopyStoreNode.create();
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return EmptyArrayNodes.ArrayCopyToNode.create();
        }

        @Override
        public ArraySortNode sortNode() {
            return EmptyArrayNodes.ArraySortNode.create();
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
        public ArrayMirror makeStorageUnshared(DynamicObject array) {
            ArrayMirror oldMirror = newMirror(array);
            ArrayMirror newMirror = oldMirror.copyArrayAndMirror(oldMirror.getLength());
            setStore(array, newMirror.getArray());
            return newMirror;
        }

        @Override
        public ArrayMirror makeStorageShared(DynamicObject array) {
            return newMirror(array);
        }

        @Override
        public ArrayMirror newArray(int size) {
            Object rawStorage = typeStrategy.newArray(size).getArray();
            DelegatedArrayStorage storage = new DelegatedArrayStorage(rawStorage, 0, size);
            return new DelegatedArrayMirror(storage, typeStrategy);
        }

        @Override
        public ArrayMirror newMirrorFromStore(Object store) {
            return new DelegatedArrayMirror((DelegatedArrayStorage) store, typeStrategy);
        }

        @TruffleBoundary
        @Override
        public String toString() {
            return String.format("Delegate of (%s)", typeStrategy);
        }

        @Override
        public ArrayGetNode getNode() {
            return DelegateArrayNodes.ArrayGetNode.create();
        }

        @Override
        public ArraySetNode setNode() {
            throw unsupported();
        }

        @Override
        public ArrayLengthNode lengthNode() {
            return DelegateArrayNodes.ArrayLengthNode.create();
        }

        @Override
        public ArrayNewStoreNode newStoreNode() {
            return DelegateArrayNodes.ArrayNewStoreNode.create(typeStrategy);
        }

        @Override
        public ArrayCopyStoreNode copyStoreNode() {
            return DelegateArrayNodes.ArrayCopyStoreNode.create(typeStrategy);
        }

        @Override
        public ArrayCopyToNode copyToNode() {
            return DelegateArrayNodes.ArrayCopyToNode.create(typeStrategy);
        }

        @Override
        public ArraySortNode sortNode() {
            return DelegateArrayNodes.ArraySortNode.create();
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
        public ArrayMirror newArray(int size) {
            throw unsupported();
        }

        @Override
        public ArrayMirror newMirrorFromStore(Object store) {
            throw unsupported();
        }

        @Override
        public String toString() {
            return "fallback";
        }

        @Override
        public ArrayLengthNode lengthNode() {
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
        public ArraySortNode sortNode() {
            throw unsupported();
        }
    }
}
