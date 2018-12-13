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
import org.truffleruby.language.NotProvided;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

public class ValueWrapperManager {

    @CompilationFinal private DynamicObject undefWrapper = null;
    @CompilationFinal private DynamicObject trueWrapper = null;
    @CompilationFinal private DynamicObject falseWrapper = null;

    private final LongHashMap<DynamicObject> longMap = new LongHashMap<>(128);
    private final LongHashMap<WeakReference<DynamicObject>> handleMap = new LongHashMap<>(1024);

    RubyContext context;

    public ValueWrapperManager(RubyContext context) {
        this.context = context;
    }

    public DynamicObject undefWrapper() {
        return undefWrapper != null ? undefWrapper : (undefWrapper = Layouts.VALUE_WRAPPER.createValueWrapper(NotProvided.INSTANCE, ValueWrapperObjectType.UNSET_HANDLE));

    }

    public DynamicObject booleanWrapper(boolean value) {
        if (value) {
            return trueWrapper != null ? trueWrapper : (trueWrapper = Layouts.VALUE_WRAPPER.createValueWrapper(true, ValueWrapperObjectType.UNSET_HANDLE));
        } else {
            return falseWrapper != null ? falseWrapper : (falseWrapper = createFalseWrapper());
        }
    }

    private DynamicObject createFalseWrapper() {
        // Ensure that Qfalse will by falsy in C.
        return Layouts.VALUE_WRAPPER.createValueWrapper(false, 0);
    }

    /*
     * We keep a map of long wrappers that have been generated because various C extensions assume
     * that any given fixnum will translate to a given VALUE.
     */
    @TruffleBoundary
    public synchronized DynamicObject longWrapper(long value) {
        DynamicObject wrapper = longMap.get(value);
        if (wrapper == null) {
            wrapper = Layouts.VALUE_WRAPPER.createValueWrapper(value, ValueWrapperObjectType.UNSET_HANDLE);
            longMap.put(value, wrapper);
        }
        return wrapper;
    }

    public DynamicObject doubleWrapper(double value) {
        return Layouts.VALUE_WRAPPER.createValueWrapper(value, ValueWrapperObjectType.UNSET_HANDLE);
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

}
