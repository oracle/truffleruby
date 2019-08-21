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

import static org.truffleruby.cext.ValueWrapperManager.FALSE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.NIL_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.TRUE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.UNDEF_HANDLE;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.UnwrapNodeGen.NativeToWrapperNodeGen;
import org.truffleruby.cext.UnwrapNodeGen.ToWrapperNodeGen;
import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@GenerateUncached
@ImportStatic({ ValueWrapperManager.class })
public abstract class UnwrapNode extends RubyBaseWithoutContextNode {

    public static UnwrapNode create() {
        return UnwrapNodeGen.create();
    }

    @GenerateUncached
    @ImportStatic(ValueWrapperManager.class)
    public static abstract class UnwrapNativeNode extends RubyBaseWithoutContextNode {

        public abstract Object execute(long handle);

        @Specialization(guards = "handle == FALSE_HANDLE")
        protected boolean unwrapFalse(long handle) {
            return false;
        }

        @Specialization(guards = "handle == TRUE_HANDLE")
        protected boolean unwrapTrue(long handle) {
            return true;
        }

        @Specialization(guards = "handle == UNDEF_HANDLE")
        protected NotProvided unwrapUndef(long handle) {
            return NotProvided.INSTANCE;
        }

        @Specialization(guards = "handle == NIL_HANDLE")
        protected DynamicObject unwrapNil(long handle,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return context.getCoreLibrary().getNil();
        }

        @Specialization(guards = "isTaggedLong(handle)")
        protected long unwrapTaggedLong(long handle) {
            return handle >> 1;
        }

        @Specialization(guards = "isTaggedObject(handle)")
        protected Object unwrapTaggedObject(long handle,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return context.getValueWrapperManager().getFromHandleMap(handle);
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

    @GenerateUncached
    @ImportStatic(ValueWrapperManager.class)
    public static abstract class NativeToWrapperNode extends RubyBaseWithoutContextNode {

        public abstract ValueWrapper execute(long handle);

        @Specialization(guards = "handle == FALSE_HANDLE")
        protected ValueWrapper unwrapFalse(long handle) {
            return new ValueWrapper(false, FALSE_HANDLE, null);
        }

        @Specialization(guards = "handle == TRUE_HANDLE")
        protected ValueWrapper unwrapTrue(long handle) {
            return new ValueWrapper(true, TRUE_HANDLE, null);
        }

        @Specialization(guards = "handle == UNDEF_HANDLE")
        protected ValueWrapper unwrapUndef(long handle) {
            return new ValueWrapper(NotProvided.INSTANCE, UNDEF_HANDLE, null);
        }

        @Specialization(guards = "handle == NIL_HANDLE")
        protected ValueWrapper unwrapNil(long handle,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return new ValueWrapper(context.getCoreLibrary().getNil(), NIL_HANDLE, null);
        }

        @Specialization(guards = "isTaggedLong(handle)")
        protected ValueWrapper unwrapTaggedLong(long handle) {
            return new ValueWrapper(null, handle, null);
        }

        @Specialization(guards = "isTaggedObject(handle)")
        protected ValueWrapper unwrapTaggedObject(long handle,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return context.getValueWrapperManager().getWrapperFromHandleMap(handle);
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

        public abstract ValueWrapper execute(Object value);

        @Specialization
        protected ValueWrapper wrappedValueWrapper(ValueWrapper value) {
            return value;
        }

        @Specialization(guards = { "!isWrapper(value)", "values.isPointer(value)" }, limit = "getCacheLimit()", rewriteOn = UnsupportedMessageException.class)
        protected ValueWrapper pointerToWrapper(Object value,
                @CachedLibrary("value") InteropLibrary values,
                @Cached NativeToWrapperNode nativeToWrapperNode) throws UnsupportedMessageException {
            return nativeToWrapperNode.execute(values.asPointer(value));
        }

        @Specialization(guards = { "!isWrapper(value)", "values.isPointer(value)" }, limit = "getCacheLimit()", replaces = "pointerToWrapper")
        protected ValueWrapper genericToWrapper(Object value,
                @CachedLibrary("value") InteropLibrary values,
                @Cached NativeToWrapperNode nativeToWrapperNode,
                @Cached BranchProfile unsupportedProfile) {
            long handle = 0;
            try {
                handle = values.asPointer(value);
            } catch (UnsupportedMessageException e) {
                unsupportedProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
            }
            return nativeToWrapperNode.execute(handle);
        }

        public static ToWrapperNode create() {
            return ToWrapperNodeGen.create();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().DISPATCH_CACHE;
        }
    }

    public abstract Object execute(Object value);

    @Specialization(guards = "!isTaggedLong(value.getHandle())")
    protected Object unwrapValueObject(ValueWrapper value) {
        return value.getObject();
    }

    @Specialization(guards = "isTaggedLong(value.getHandle())")
    protected long unwrapValueTaggedLong(ValueWrapper value) {
        return value.getHandle() >> 1;
    }

    @Specialization(guards = { "!isWrapper(value)", "values.isPointer(value)" }, limit = "getCacheLimit()", rewriteOn = UnsupportedMessageException.class)
    protected Object unwrapPointer(Object value,
            @CachedLibrary("value") InteropLibrary values,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached UnwrapNativeNode unwrapNativeNode) throws UnsupportedMessageException {
        return unwrapNativeNode.execute(values.asPointer(value));
    }

    @Specialization(guards = { "!isWrapper(value)", "values.isPointer(value)" }, limit = "getCacheLimit()", replaces = "unwrapPointer")
    protected Object unwrapGeneric(Object value,
            @CachedLibrary("value") InteropLibrary values,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached UnwrapNativeNode unwrapNativeNode,
            @Cached BranchProfile unsupportedProfile) {
        long handle = 0;
        try {
            handle = values.asPointer(value);
        } catch (UnsupportedMessageException e) {
            unsupportedProfile.enter();
            throw new RaiseException(context, context.getCoreExceptions().argumentError(e.getMessage(), this, e));
        }
        return unwrapNativeNode.execute(handle);
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().DISPATCH_CACHE;
    }
}
