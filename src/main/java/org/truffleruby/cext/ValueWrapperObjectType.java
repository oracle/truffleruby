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

import org.truffleruby.Layouts;
import org.truffleruby.collections.LongHashMap;
import org.truffleruby.language.NotProvided;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;

public class ValueWrapperObjectType extends ObjectType {

    private static DynamicObject UNDEF_WRAPPER = null;

    private static DynamicObject TRUE_WRAPPER = null;
    private static DynamicObject FALSE_WRAPPER = null;

    private static LongHashMap<DynamicObject> longMap = new LongHashMap<>(128);

    public static DynamicObject createValueWrapper(Object value) {
        return Layouts.VALUE_WRAPPER.createValueWrapper(value);
    }

    public static synchronized DynamicObject createUndefWrapper(NotProvided value) {
        return UNDEF_WRAPPER != null ? UNDEF_WRAPPER : (UNDEF_WRAPPER = Layouts.VALUE_WRAPPER.createValueWrapper(value));
    }

    public static synchronized DynamicObject createBooleanWrapper(boolean value) {
        if (value) {
            return TRUE_WRAPPER != null ? TRUE_WRAPPER : (TRUE_WRAPPER = Layouts.VALUE_WRAPPER.createValueWrapper(true));
        } else {
            return FALSE_WRAPPER != null ? FALSE_WRAPPER : (FALSE_WRAPPER = createFalseWrapper());
        }
    }

    private static DynamicObject createFalseWrapper() {
        // Ensure that Qfalse will by falsy in C.
        return Layouts.VALUE_WRAPPER.createValueWrapper(false);
    }

    /*
     * We keep a map of long wrappers that have been generated because various C extensions assume
     * that any given fixnum will translate to a given VALUE.
     */
    @TruffleBoundary
    public static synchronized DynamicObject createLongWrapper(long value) {
        DynamicObject wrapper = longMap.get(value);
        if (wrapper == null) {
            wrapper = Layouts.VALUE_WRAPPER.createValueWrapper(value);
            longMap.put(value, wrapper);
        }
        return wrapper;
    }

    @TruffleBoundary
    public static synchronized DynamicObject createDoubleWrapper(double value) {
        return Layouts.VALUE_WRAPPER.createValueWrapper(value);
    }

    public static boolean isInstance(TruffleObject receiver) {
        return Layouts.VALUE_WRAPPER.isValueWrapper(receiver);
    }

}
