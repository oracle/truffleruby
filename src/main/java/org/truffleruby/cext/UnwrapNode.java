/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import static org.truffleruby.cext.ValueWrapperManager.FALSE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.TRUE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.UNDEF_HANDLE;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;

@GenerateUncached
@GenerateInline
@GenerateCached(false)
@ImportStatic(ValueWrapperManager.class)
public abstract class UnwrapNode extends RubyBaseNode {

    @GenerateUncached
    @ImportStatic(ValueWrapperManager.class)
    public abstract static class UnwrapNativeNode extends RubyBaseNode {

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
        protected Object unwrapNil(long handle) {
            return nil;
        }

        @Specialization(guards = "isTaggedLong(handle)")
        protected long unwrapTaggedLong(long handle) {
            return ValueWrapperManager.untagTaggedLong(handle);
        }

        @Specialization(guards = "isTaggedObject(handle)")
        protected Object unwrapTaggedObject(long handle,
                @Cached InlinedBranchProfile noHandleProfile) {
            final ValueWrapper wrapper = getContext()
                    .getValueWrapperManager()
                    .getWrapperFromHandleMap(handle, getLanguage());
            if (wrapper == null) {
                noHandleProfile.enter(this);
                raiseError(handle);
            }
            return wrapper.getObject();
        }

        @Fallback
        protected ValueWrapper unWrapUnexpectedHandle(long handle) {
            // Avoid throwing a specialization exception when given an uninitialized or corrupt
            // handle.
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw CompilerDirectives.shouldNotReachHere("corrupt handle 0x" + Long.toHexString(handle));
        }

        @TruffleBoundary
        private void raiseError(long handle) {
            throw CompilerDirectives.shouldNotReachHere("dead handle 0x" + Long.toHexString(handle));
        }
    }

    @GenerateUncached
    @ImportStatic(ValueWrapperManager.class)
    public abstract static class NativeToWrapperNode extends RubyBaseNode {

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
        protected ValueWrapper unwrapNil(long handle) {
            return nil.getValueWrapper();
        }

        @Specialization(guards = "isTaggedLong(handle)")
        protected ValueWrapper unwrapTaggedLong(long handle) {
            return new ValueWrapper(null, handle, null);
        }

        @Specialization(guards = "isTaggedObject(handle)")
        protected ValueWrapper unwrapTaggedObject(long handle) {
            return getContext().getValueWrapperManager().getWrapperFromHandleMap(handle, getLanguage());
        }

        @Fallback
        protected ValueWrapper unWrapUnexpectedHandle(long handle) {
            // Avoid throwing a specialization exception when given an uninitialized or corrupt
            // handle.
            return null;
        }
    }

    @ImportStatic(ValueWrapperManager.class)
    public abstract static class ToWrapperNode extends RubyBaseNode {

        public abstract ValueWrapper execute(Object value);

        @Specialization
        protected ValueWrapper wrappedValueWrapper(ValueWrapper value) {
            return value;
        }

        @Specialization
        protected ValueWrapper longToWrapper(long value,
                @Cached @Shared NativeToWrapperNode nativeToWrapperNode) {
            return nativeToWrapperNode.execute(value);
        }

        @Fallback
        protected ValueWrapper genericToWrapper(Object value,
                @CachedLibrary(limit = "getCacheLimit()") InteropLibrary values,
                @Cached @Shared NativeToWrapperNode nativeToWrapperNode,
                @Cached InlinedBranchProfile unsupportedProfile) {
            long handle;
            try {
                handle = values.asPointer(value);
            } catch (UnsupportedMessageException e) {
                unsupportedProfile.enter(this);
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
            }
            return nativeToWrapperNode.execute(handle);
        }

        protected int getCacheLimit() {
            return getLanguage().options.DISPATCH_CACHE;
        }
    }

    @ImportStatic(ValueWrapperManager.class)
    public abstract static class UnwrapCArrayNode extends RubyBaseNode {

        public abstract Object[] execute(Object cArray);

        @ExplodeLoop
        @Specialization(
                guards = { "size == cachedSize", "cachedSize <= MAX_EXPLODE_SIZE" },
                limit = "1")
        protected Object[] unwrapCArrayExplode(Object cArray,
                @CachedLibrary("cArray") InteropLibrary interop,
                @Bind("getArraySize(cArray, interop)") int size,
                @Cached("size") int cachedSize,
                @Cached @Shared UnwrapNode unwrapNode) {
            final Object[] store = new Object[cachedSize];
            for (int i = 0; i < cachedSize; i++) {
                final Object cValue = readArrayElement(cArray, interop, i);
                store[i] = unwrapNode.execute(this, cValue);
            }
            return store;
        }

        @Specialization(replaces = "unwrapCArrayExplode", limit = "getDefaultCacheLimit()")
        protected Object[] unwrapCArray(Object cArray,
                @CachedLibrary("cArray") InteropLibrary interop,
                @Bind("getArraySize(cArray, interop)") int size,
                @Cached @Shared UnwrapNode unwrapNode,
                @Cached LoopConditionProfile loopProfile) {
            final Object[] store = new Object[size];
            int i = 0;
            try {
                for (; loopProfile.inject(i < size); i++) {
                    final Object cValue = readArrayElement(cArray, interop, i);
                    store[i] = unwrapNode.execute(this, cValue);
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, i);
            }
            return store;
        }

        protected static int getArraySize(Object cArray, InteropLibrary interop) {
            try {
                return Math.toIntExact(interop.getArraySize(cArray));
            } catch (UnsupportedMessageException | ArithmeticException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
        }

        private static Object readArrayElement(Object cArray, InteropLibrary interop, int i) {
            try {
                return interop.readArrayElement(cArray, i);
            } catch (UnsupportedMessageException | InvalidArrayIndexException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
        }
    }

    public abstract Object execute(Node node, Object value);

    @Specialization(guards = "!isTaggedLong(value.getHandle())")
    protected static Object unwrapValueObject(ValueWrapper value) {
        return value.getObject();
    }

    @Specialization(guards = "isTaggedLong(value.getHandle())")
    protected static long unwrapValueTaggedLong(ValueWrapper value) {
        return ValueWrapperManager.untagTaggedLong(value.getHandle());
    }

    @Specialization
    protected static Object longToWrapper(long value,
            @Cached @Shared UnwrapNativeNode unwrapNativeNode) {
        return unwrapNativeNode.execute(value);
    }

    @Specialization(guards = { "!isWrapper(value)", "!isImplicitLong(value)" })
    protected static Object unwrapGeneric(Node node, Object value,
            @CachedLibrary(limit = "getCacheLimit()") InteropLibrary values,
            @Cached @Shared UnwrapNativeNode unwrapNativeNode,
            @Cached InlinedBranchProfile unsupportedProfile) {
        long handle;
        try {
            handle = values.asPointer(value);
        } catch (UnsupportedMessageException e) {
            unsupportedProfile.enter(node);
            throw new RaiseException(getContext(node), coreExceptions(node).argumentError(e.getMessage(), node, e));
        }
        return unwrapNativeNode.execute(handle);
    }

    protected int getCacheLimit() {
        return getLanguage().options.DISPATCH_CACHE;
    }
}
