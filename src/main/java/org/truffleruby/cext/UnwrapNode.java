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

import static org.truffleruby.cext.ValueWrapperManager.TAG_MASK;
import static org.truffleruby.cext.ValueWrapperManager.TRUE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.UNDEF_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.LONG_TAG;
import static org.truffleruby.cext.ValueWrapperManager.NIL_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.OBJECT_TAG;
import static org.truffleruby.cext.ValueWrapperManager.FALSE_HANDLE;

import org.truffleruby.cext.UnwrapNodeGen.NativeToWrapperNodeGen;
import org.truffleruby.cext.UnwrapNodeGen.ToWrapperNodeGen;
import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@ImportStatic(Message.class)
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

        public boolean isTaggedLong(long handle) {
            return (handle & LONG_TAG) == LONG_TAG;
        }

        public boolean isTaggedObject(long handle) {
            return handle != FALSE_HANDLE && (handle & TAG_MASK) == OBJECT_TAG;
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

        public boolean isTaggedLong(long handle) {
            return (handle & LONG_TAG) == LONG_TAG;
        }

        public boolean isTaggedObject(long handle) {
            return handle != FALSE_HANDLE && (handle & TAG_MASK) == OBJECT_TAG;
        }

        public static NativeToWrapperNode create() {
            return NativeToWrapperNodeGen.create();
        }
    }

    @ImportStatic(Message.class)
    public static abstract class ToWrapperNode extends RubyBaseNode {

        public abstract ValueWrapper execute(Object value);

        @Specialization
        public ValueWrapper wrappedValueWrapper(ValueWrapper value) {
            return value;
        }

        @Specialization(guards = "!isWrapper(value)")
        public ValueWrapper unwrapTypeCastObject(TruffleObject value,
                @Cached("IS_POINTER.createNode()") Node isPointerNode,
                @Cached("AS_POINTER.createNode()") Node asPointerNode,
                @Cached("create()") NativeToWrapperNode nativeToWrapperNode,
                @Cached("create()") BranchProfile unsupportedProfile,
                @Cached("create()") BranchProfile nonPointerProfile) {
            if (ForeignAccess.sendIsPointer(isPointerNode, value)) {
                long handle = 0;
                try {
                    handle = ForeignAccess.sendAsPointer(asPointerNode, value);
                } catch (UnsupportedMessageException e) {
                    unsupportedProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
                }
                return nativeToWrapperNode.execute(handle);
            } else {
                nonPointerProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError("Not a handle or a pointer", this));
            }
        }

        public static boolean isWrapper(TruffleObject value) {
            return value instanceof ValueWrapper;
        }

        public static ToWrapperNode create() {
            return ToWrapperNodeGen.create();
        }
    }

    public abstract Object execute(Object value);

    @Specialization
    public Object unwrapValue(ValueWrapper value) {
        return value.getObject();
    }

    @Specialization(guards = "!isWrapper(value)")
    public Object unwrapTypeCastObject(TruffleObject value,
            @Cached("IS_POINTER.createNode()") Node isPointerNode,
            @Cached("AS_POINTER.createNode()") Node asPointerNode,
            @Cached("create()") UnwrapNativeNode unwrapNativeNode,
            @Cached("create()") BranchProfile unsupportedProfile,
            @Cached("create()") BranchProfile nonPointerProfile) {
        if (ForeignAccess.sendIsPointer(isPointerNode, value)) {
            long handle = 0;
            try {
                handle = ForeignAccess.sendAsPointer(asPointerNode, value);
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

    public static boolean isWrapper(TruffleObject value) {
        return value instanceof ValueWrapper;
    }
}
