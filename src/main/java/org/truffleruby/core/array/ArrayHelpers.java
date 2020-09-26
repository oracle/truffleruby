/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.library.ArrayStoreLibrary;

public abstract class ArrayHelpers {

    public static void setStoreAndSize(RubyArray array, Object store, int size) {
        array.store = store;
        setSize(array, size);
    }

    /** Sets the size of the given array
     *
     * Asserts that the size is valid for the current store of the array. If setting both size and store, use
     * setStoreAndSize or be sure to setStore before setSize as this assertion may fail. */
    public static void setSize(RubyArray array, int size) {
        assert ArrayOperations.getStoreCapacity(array) >= size;
        array.size = size;
    }

    public static RubyArray createArray(RubyContext context, Object store, int size) {
        assert !(store instanceof Object[]) || store.getClass() == Object[].class;
        return new RubyArray(context.getCoreLibrary().arrayClass, RubyLanguage.arrayShape, store, size);
    }

    public static RubyArray createArray(RubyContext context, int[] store) {
        return createArray(context, store, store.length);
    }

    public static RubyArray createArray(RubyContext context, long[] store) {
        return createArray(context, store, store.length);
    }

    public static RubyArray createArray(RubyContext context, Object[] store) {
        assert store.getClass() == Object[].class;
        return createArray(context, store, store.length);
    }

    public static RubyArray createEmptyArray(RubyContext context) {
        return new RubyArray(
                context.getCoreLibrary().arrayClass,
                RubyLanguage.arrayShape,
                ArrayStoreLibrary.INITIAL_STORE,
                0);
    }

    /** Returns a Java array of the narrowest possible type holding {@code object}. */
    public static Object specializedJavaArrayOf(ArrayBuilderNode builder, Object object) {
        final ArrayBuilderNode.BuilderState state = builder.start(1);
        builder.appendValue(state, 0, object);
        return builder.finish(state, 1);
    }

    /** Returns a Java array of the narrowest possible type holding the {@code objects}. */
    public static Object specializedJavaArrayOf(ArrayBuilderNode builder, Object... objects) {
        final ArrayBuilderNode.BuilderState state = builder.start(objects.length);
        for (Object object : objects) {
            builder.appendValue(state, 0, object);
        }
        return builder.finish(state, objects.length);
    }

    /** Returns a Ruby array backed by a store of the narrowest possible type, holding {@code object}. */
    public static RubyArray specializedRubyArrayOf(RubyContext context, ArrayBuilderNode builder, Object object) {
        return createArray(context, specializedJavaArrayOf(builder, object), 1);
    }

    /** Returns a Ruby array backed by a store of the narrowest possible type, holding the {@code objects}. */
    public static RubyArray specializedRubyArrayOf(RubyContext context, ArrayBuilderNode builder,
            Object... objects) {
        return createArray(context, specializedJavaArrayOf(builder, objects), objects.length);
    }

}
