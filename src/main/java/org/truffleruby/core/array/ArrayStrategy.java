/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.language.RubyGuards;

public abstract class ArrayStrategy {

    // ArrayStrategy interface

    protected Class<?> type() {
        throw unsupported();
    }

    public boolean canStore(Class<?> type) {
        throw unsupported();
    }

    public abstract boolean accepts(Object value);

    public abstract boolean isPrimitive();

    public abstract boolean isStorageMutable();

    public boolean specializesFor(Object value) {
        throw unsupported();
    }

    public boolean isDefaultValue(Object value) {
        throw unsupported();
    }

    public final boolean matches(DynamicObject array) {
        return matchesStore(Layouts.ARRAY.getStore(array));
    }

    public abstract boolean matchesStore(Object store);

    public int getSize(DynamicObject array) {
        return Layouts.ARRAY.getSize(array);
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

    public ArrayStrategy generalize(ArrayStrategy other) {
        CompilerAsserts.neverPartOfCompilation();
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

    public ArrayStrategy generalizeNew(ArrayStrategy other) {
        return generalize(other);
    }

    public ArrayStrategy generalizeForMutation() {
        return this;
    }

    public ArrayStrategy generalizeFor(Object value) {
        return generalize(ArrayStrategy.forValue(value));
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

    public static ArrayStrategy ofStore(Object store) {
        CompilerAsserts.neverPartOfCompilation();

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

    public static ArrayStrategy of(DynamicObject array) {
        CompilerAsserts.neverPartOfCompilation();

        if (!RubyGuards.isRubyArray(array)) {
            return FallbackArrayStrategy.INSTANCE;
        }

        return ofStore(Layouts.ARRAY.getStore(array));
    }

    public static ArrayStrategy forValue(Object value) {
        CompilerAsserts.neverPartOfCompilation();
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
        public ArrayStrategy generalize(ArrayStrategy other) {
            CompilerAsserts.neverPartOfCompilation();
            if (other == this) {
                return this;
            } else if (other == LongArrayStrategy.INSTANCE) {
                return LongArrayStrategy.INSTANCE;
            } else {
                return IntToObjectGeneralizationArrayStrategy.INSTANCE;
            }
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

    }

    /** Object[] not accepting long */
    private static class IntToObjectGeneralizationArrayStrategy extends ArrayStrategy {

        static final ArrayStrategy INSTANCE = new IntToObjectGeneralizationArrayStrategy();

        @Override
        public boolean accepts(Object value) {
            return !(value instanceof Long);
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
        public boolean matchesStore(Object store) {
            return store != null && store.getClass() == Object[].class;
        }

        @Override
        public ArrayStrategy generalize(ArrayStrategy other) {
            CompilerAsserts.neverPartOfCompilation();
            if (other == LongArrayStrategy.INSTANCE) {
                return ObjectArrayStrategy.INSTANCE;
            } else if (other == ObjectArrayStrategy.INSTANCE) {
                return other;
            } else {
                return this;
            }
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
            return "Object[] (not accepting long)";
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
            return type == null;
        }

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
            return true;
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
            throw new UnsupportedOperationException();
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

    }
}
