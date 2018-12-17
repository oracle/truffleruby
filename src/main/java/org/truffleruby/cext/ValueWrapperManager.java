/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import java.lang.ref.WeakReference;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.LongHashMap;
import org.truffleruby.extra.ffi.Pointer;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

public class ValueWrapperManager {

    static final int UNSET_HANDLE = -1;

    /*
     * These constants are taken from ruby.h, and are based on us not tagging doubles.
     */

    public static final int FALSE_HANDLE = 0x0;
    public static final int TRUE_HANDLE = 0x2;
    public static final int NIL_HANDLE = 0x04;
    public static final int UNDEF_HANDLE = 0x6;

    public static final long LONG_TAG = 1;
    public static final long OBJECT_TAG = 0;

    public static final long MIN_FIXNUM_VALUE = -(1L << 62);
    public static final long MAX_FIXNUM_VALUE = (1L << 62) - 1;

    public static final int TAG_BITS = 3;
    public static final long TAG_MASK = 0x7;

    private final LongHashMap<WeakReference<DynamicObject>> handleMap = new LongHashMap<>(1024);

    private final RubyContext context;

    public ValueWrapperManager(RubyContext context) {
        this.context = context;
    }

    /*
     * We keep a map of long wrappers that have been generated because various C extensions assume
     * that any given fixnum will translate to a given VALUE.
     */
    @TruffleBoundary
    public synchronized DynamicObject longWrapper(long value) {
        return Layouts.VALUE_WRAPPER.createValueWrapper(value, UNSET_HANDLE);
    }

    public DynamicObject doubleWrapper(double value) {
        return Layouts.VALUE_WRAPPER.createValueWrapper(value, UNSET_HANDLE);
    }

    @TruffleBoundary
    public synchronized void addToHandleMap(long handle, DynamicObject wrapper) {
        handleMap.put(handle, new WeakReference<>(wrapper));
    }

    @TruffleBoundary
    public synchronized Object getFromHandleMap(long handle) {
        WeakReference<DynamicObject> ref = handleMap.get(handle);
        DynamicObject object;
        if (ref == null) {
            return null;
        }
        if ((object = ref.get()) == null) {
            return null;
        }
        return Layouts.VALUE_WRAPPER.getObject(object);
    }

    @TruffleBoundary
    public synchronized void removeFromHandleMap(long handle) {
        handleMap.remove(handle);
    }

    @TruffleBoundary
    public synchronized long createNativeHandle(DynamicObject wrapper) {
        Pointer handlePointer = Pointer.malloc(1);
        long handleAddress = handlePointer.getAddress();
        if ((handleAddress & TAG_MASK) != 0) {
            throw new RuntimeException("unaligned malloc for native handle");
        }
        Layouts.VALUE_WRAPPER.setHandle(wrapper, handleAddress);
        addToHandleMap(handleAddress, wrapper);
        addFinalizer(wrapper, handlePointer);
        return handleAddress;
    }

    public void addFinalizer(DynamicObject wrapper, Pointer handle) {
        context.getFinalizationService().addFinalizer(
                wrapper, null, ValueWrapperObjectType.class,
                createFinalizer(handle), null);
    }

    private Runnable createFinalizer(Pointer handle) {
        return () -> {
            this.removeFromHandleMap(handle.getAddress());
            handle.free();
        };

    }
}
