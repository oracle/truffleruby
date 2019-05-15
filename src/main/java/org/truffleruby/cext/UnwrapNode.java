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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.cext.UnwrapNodeGen.NativeToWrapperNodeGen;
import org.truffleruby.cext.UnwrapNodeGen.ToWrapperNodeGen;
import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import static org.truffleruby.cext.ValueWrapperManager.FALSE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.NIL_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.TRUE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.UNDEF_HANDLE;

@ImportStatic({ ValueWrapperManager.class })
public abstract class UnwrapNode extends RubyBaseNode {

    @ImportStatic(ValueWrapperManager.class)
    public static abstract class UnwrapNativeNode extends RubyBaseNode {

        public abstract Object execute(long handle);

        @Specialization(guards = "handle == FALSE_HANDLE")
        public boolean unwrapFalse(long handle) {
            return false;
        }

        @Specialization(guards = "handle == TRUE_HANDLE")
        public boolean unwrapTrue(long handle) {
            return true;
        }

        @Specialization(guards = "handle == UNDEF_HANDLE")
        public NotProvided unwrapUndef(long handle) {
            return NotProvided.INSTANCE;
        }

        @Specialization(guards = "handle == NIL_HANDLE")
        public DynamicObject unwrapNil(long handle) {
            return nil();
        }

        @Specialization(guards = "isTaggedLong(handle)")
        public long unwrapTaggedLong(long handle) {
            return handle >> 1;
        }

        @Specialization(guards = "isTaggedObject(handle)")
        public Object unwrapTaggedObject(long handle) {
            return getContext().getValueWrapperManager().getFromHandleMap(handle);
        }

        @Fallback
        public ValueWrapper unWrapUnexpectedHandle(long handle) {
            // Avoid throwing a specialization exception when given an uninitialized or corrupt
            // handle.
            return null;
        }

        public static UnwrapNativeNode create() {
            return UnwrapNativeNodeGen.create();
        }
    }

    @ImportStatic(ValueWrapperManager.class)
    public static abstract class NativeToWrapperNode extends RubyBaseNode {

        public abstract ValueWrapper execute(long handle);

        @Specialization(guards = "handle == FALSE_HANDLE")
        public ValueWrapper unwrapFalse(long handle) {
            return new ValueWrapper(false, FALSE_HANDLE);
        }

        @Specialization(guards = "handle == TRUE_HANDLE")
        public ValueWrapper unwrapTrue(long handle) {
            return new ValueWrapper(true, TRUE_HANDLE);
        }

        @Specialization(guards = "handle == UNDEF_HANDLE")
        public ValueWrapper unwrapUndef(long handle) {
            return new ValueWrapper(NotProvided.INSTANCE, UNDEF_HANDLE);
        }

        @Specialization(guards = "handle == NIL_HANDLE")
        public ValueWrapper unwrapNil(long handle) {
            return new ValueWrapper(nil(), NIL_HANDLE);
        }

        @Specialization(guards = "isTaggedLong(handle)")
        public ValueWrapper unwrapTaggedLong(long handle) {
            return new ValueWrapper(handle >> 1, handle);
        }

        @Specialization(guards = "isTaggedObject(handle)")
        public ValueWrapper unwrapTaggedObject(long handle) {
            return getContext().getValueWrapperManager().getWrapperFromHandleMap(handle);
        }

        @Fallback
        public ValueWrapper unWrapUnexpectedHandle(long handle) {
            // Avoid throwing a specialization exception when given an uninitialized or corrupt
            // handle.
            return null;
        }

        public static NativeToWrapperNode create() {
            return NativeToWrapperNodeGen.create();
        }
    }

    @ImportStatic({ ValueWrapperManager.class })
    public static abstract class ToWrapperNode extends RubyBaseNode {

        public abstract ValueWrapper execute(TruffleObject value);

        @Specialization
        public ValueWrapper wrappedValueWrapper(ValueWrapper value) {
            return value;
        }

        @Specialization(guards = "!isWrapper(value)", limit = "getCacheLimit()")
        public ValueWrapper unwrapTypeCastObject(TruffleObject value,
                @CachedLibrary("value") InteropLibrary values,
                @Cached("create()") NativeToWrapperNode nativeToWrapperNode,
                @Cached("create()") BranchProfile unsupportedProfile,
                @Cached("create()") BranchProfile nonPointerProfile) {
            if (values.isPointer(value)) {
                long handle = 0;
                try {
                    handle = values.asPointer(value);
                } catch (UnsupportedMessageException e) {
                    unsupportedProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
                }
                return nativeToWrapperNode.execute(handle);
            } else {
                nonPointerProfile.enter();
                return null;
            }
        }

        public static ToWrapperNode create() {
            return ToWrapperNodeGen.create();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().DISPATCH_CACHE;
        }
    }

    public abstract Object execute(TruffleObject value);

    @Specialization
    public Object unwrapValue(ValueWrapper value) {
        return value.getObject();
    }

    @Specialization(guards = "!isWrapper(value)", limit = "getCacheLimit()")
    public Object unwrapTypeCastObject(TruffleObject value,
            @CachedLibrary("value") InteropLibrary values,
            @Cached("create()") UnwrapNativeNode unwrapNativeNode,
            @Cached("create()") BranchProfile unsupportedProfile,
            @Cached("create()") BranchProfile nonPointerProfile) {
        if (values.isPointer(value)) {
            long handle = 0;
            try {
                handle = values.asPointer(value);
            } catch (UnsupportedMessageException e) {
                unsupportedProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
            }
            return unwrapNativeNode.execute(handle);
        } else {
            nonPointerProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().argumentError("Not a handle or a pointer", this));
        }
    }

    protected int getCacheLimit() {
        return getContext().getOptions().DISPATCH_CACHE;
    }
}
