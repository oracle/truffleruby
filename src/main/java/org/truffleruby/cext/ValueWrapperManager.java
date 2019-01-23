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

import org.truffleruby.RubyContext;
import org.truffleruby.collections.LongHashMap;
import org.truffleruby.extra.ffi.Pointer;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.TruffleObject;

public class ValueWrapperManager {

    static final int UNSET_HANDLE = -1;

    /*
     * These constants are taken from ruby.h, and are based on us not tagging doubles.
     */

    public static final int FALSE_HANDLE = 0b000;
    public static final int TRUE_HANDLE = 0b010;
    public static final int NIL_HANDLE = 0b100;
    public static final int UNDEF_HANDLE = 0b110;

    public static final long LONG_TAG = 1;
    public static final long OBJECT_TAG = 0;

    public static final long MIN_FIXNUM_VALUE = -(1L << 62);
    public static final long MAX_FIXNUM_VALUE = (1L << 62) - 1;

    public static final long TAG_MASK = 0b111;

    private final LongHashMap<WeakReference<ValueWrapper>> handleMap = new LongHashMap<>(1024);

    private final RubyContext context;

    public ValueWrapperManager(RubyContext context) {
        this.context = context;
    }

    /*
     * We keep a map of long wrappers that have been generated because various C extensions assume
     * that any given fixnum will translate to a given VALUE.
     */
    public ValueWrapper longWrapper(long value) {
        return new ValueWrapper(value, UNSET_HANDLE);
    }

    public ValueWrapper doubleWrapper(double value) {
        return new ValueWrapper(value, UNSET_HANDLE);
    }

    @TruffleBoundary
    public synchronized void addToHandleMap(long handle, ValueWrapper wrapper) {
        handleMap.put(handle, new WeakReference<>(wrapper));
    }

    @TruffleBoundary
    public synchronized Object getFromHandleMap(long handle) {
        WeakReference<ValueWrapper> ref = handleMap.get(handle);
        ValueWrapper wrapper;
        if (ref == null) {
            return null;
        }
        if ((wrapper = ref.get()) == null) {
            return null;
        }
        return wrapper.getObject();
    }

    @TruffleBoundary
    public synchronized ValueWrapper getWrapperFromHandleMap(long handle) {
        WeakReference<ValueWrapper> ref = handleMap.get(handle);
        ValueWrapper wrapper;
        if (ref == null) {
            return null;
        }
        if ((wrapper = ref.get()) == null) {
            return null;
        }
        return wrapper;
    }

    @TruffleBoundary
    public synchronized void removeFromHandleMap(long handle) {
        handleMap.remove(handle);
    }

    @TruffleBoundary
    public synchronized long createNativeHandle(ValueWrapper wrapper) {
        Pointer handlePointer = Pointer.malloc(1);
        long handleAddress = handlePointer.getAddress();
        if ((handleAddress & TAG_MASK) != 0) {
            throw new RuntimeException("unaligned malloc for native handle");
        }
        wrapper.setHandle(handleAddress);
        addToHandleMap(handleAddress, wrapper);
        context.getMarkingService().keepObject(wrapper);
        addFinalizer(wrapper, handlePointer);
        return handleAddress;
    }

    public void addFinalizer(ValueWrapper wrapper, Pointer handle) {
        context.getFinalizationService().addFinalizer(
                wrapper, null, ValueWrapper.class,
                createFinalizer(handle), null);
    }

    private Runnable createFinalizer(Pointer handle) {
        return () -> {
            this.removeFromHandleMap(handle.getAddress());
            handle.free();
        };

    }

    public static boolean isTaggedLong(long handle) {
        return (handle & LONG_TAG) == LONG_TAG;
    }

    public static boolean isTaggedObject(long handle) {
        return handle != FALSE_HANDLE && (handle & TAG_MASK) == OBJECT_TAG;
    }

    public static boolean isWrapper(TruffleObject value) {
        return value instanceof ValueWrapper;
    }
}
