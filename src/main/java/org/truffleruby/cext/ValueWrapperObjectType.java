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

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.collections.LongHashMap;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;

public class ValueWrapperObjectType extends ObjectType {

    public static final ValueWrapperLayout VALUE_WRAPPER = ValueWrapperLayoutImpl.INSTANCE;

    private static DynamicObject UNDEF_WRAPPER = null;

    private static DynamicObject TRUE_WRAPPER = null;
    private static DynamicObject FALSE_WRAPPER = null;

    private static LongHashMap<DynamicObject> longMap = new LongHashMap<>(128);

    public static DynamicObject createValueWrapper(Object value) {
        return VALUE_WRAPPER.createValueWrapper(value);
    }

    public static synchronized DynamicObject createUndefWrapper(NotProvided value) {
        return UNDEF_WRAPPER != null ? UNDEF_WRAPPER : (UNDEF_WRAPPER = VALUE_WRAPPER.createValueWrapper(value));
    }

    public static synchronized DynamicObject createBooleanWrapper(boolean value) {
        if (value) {
            return TRUE_WRAPPER != null ? TRUE_WRAPPER : (TRUE_WRAPPER = VALUE_WRAPPER.createValueWrapper(true));
        } else {
            return FALSE_WRAPPER != null ? FALSE_WRAPPER : (FALSE_WRAPPER = createFalseWrapper());
        }
    }

    protected static DynamicObject createFalseWrapper() {
        // Ensure that Qfalse will by falsy in C.
        final DynamicObject falseWrapper = VALUE_WRAPPER.createValueWrapper(false);
        return falseWrapper;
    }

    /*
     * We keep a map of long wrappers that have been generated because various C extensions assume
     * that any given fixnum will translate to a given VALUE.
     */
    @TruffleBoundary
    public static synchronized DynamicObject createLongWrapper(long value) {
        DynamicObject wrapper = longMap.get(value);
        if (wrapper == null) {
            wrapper = VALUE_WRAPPER.createValueWrapper(value);
            longMap.put(value, wrapper);
        }
        return wrapper;
    }

    @TruffleBoundary
    public static synchronized DynamicObject createDoubleWrapper(double value) {
        return VALUE_WRAPPER.createValueWrapper(value);
    }

    public static boolean isInstance(TruffleObject receiver) {
        return VALUE_WRAPPER.isValueWrapper(receiver);
    }

    public static abstract class WrapNode extends RubyBaseNode {

        private static Object WRAPPER_VAR = new Object();

        public abstract TruffleObject execute(Object value);

        @Specialization
        public DynamicObject wrapInt(int value) {
            return createLongWrapper(value);
        }

        @Specialization
        public DynamicObject wrapLong(long value) {
            return createLongWrapper(value);
        }

        @Specialization
        public DynamicObject wrapDouble(double value) {
            return createDoubleWrapper(value);
        }

        @Specialization
        public DynamicObject wrapBoolean(boolean value) {
            return createBooleanWrapper(value);
        }

        @Specialization
        public DynamicObject wrapUndef(NotProvided value) {
            return createUndefWrapper(value);
        }

        @Specialization(guards = "isWrapped(value)")
        public DynamicObject wrapWrappedValue(DynamicObject value) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(RopeOperations.encodeAscii("Wrapping wrapped object.", UTF8Encoding.INSTANCE), this));
        }

        @Specialization(guards = "isRubyBasicObject(value)")
        public DynamicObject wrapValue(DynamicObject value,
                @Cached("createReader()") ReadObjectFieldNode readWrapperNode,
                @Cached("createWriter()") WriteObjectFieldNode writeWrapperNode) {
            DynamicObject wrapper = (DynamicObject) readWrapperNode.execute(value);
            if (wrapper == null) {
                synchronized (value) {
                    wrapper = (DynamicObject) readWrapperNode.execute(value);
                    if (wrapper == null) {
                        wrapper = ValueWrapperObjectType.createValueWrapper(value);
                        writeWrapperNode.write(value, wrapper);
                    }
                }
            }
            return wrapper;
        }

        @Specialization(guards = "!isRubyBasicObject(value)")
        public TruffleObject wrapNonRubyObject(TruffleObject value) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("Attempt to wrap something that isn't an Ruby object", this));
        }

        public ReadObjectFieldNode createReader() {
            return ReadObjectFieldNodeGen.create(WRAPPER_VAR, null);
        }

        public WriteObjectFieldNode createWriter() {
            return WriteObjectFieldNodeGen.create(WRAPPER_VAR);
        }

        public boolean isWrapped(TruffleObject value) {
            return ValueWrapperObjectType.isInstance(value);
        }

    }

    public static abstract class UnwrapNode extends RubyBaseNode {

        public abstract Object execute(Object value);

        @Specialization(guards = "isWrapper(value)")
        public Object unwrapValue(DynamicObject value) {
            return ValueWrapperObjectType.VALUE_WRAPPER.getObject(value);
        }

        @Fallback
        public Object unwrapTypeCastObject(Object value) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("Unwrapping something that isn't a wrapper", this));
        }

        public static boolean isWrapper(TruffleObject value) {
            return ValueWrapperObjectType.isInstance(value);
        }
    }

}
