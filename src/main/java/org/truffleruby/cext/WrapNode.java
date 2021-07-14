/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import static org.truffleruby.cext.ValueWrapperManager.LONG_TAG;
import static org.truffleruby.cext.ValueWrapperManager.UNSET_HANDLE;

import com.oracle.truffle.api.memory.MemoryFence;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;

@GenerateUncached
@ImportStatic(RubyGuards.class)
public abstract class WrapNode extends RubyBaseNode {

    public static WrapNode create() {
        return WrapNodeGen.create();
    }

    public abstract ValueWrapper execute(Object value);

    @Specialization
    protected ValueWrapper wrapLong(long value,
            @Cached BranchProfile smallFixnumProfile,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        if (value >= ValueWrapperManager.MIN_FIXNUM_VALUE && value <= ValueWrapperManager.MAX_FIXNUM_VALUE) {
            smallFixnumProfile.enter();
            long val = (value << 1) | LONG_TAG;
            return new ValueWrapper(null, val, null);
        } else {
            return context.getValueWrapperManager().longWrapper(value);
        }
    }

    @Specialization
    protected ValueWrapper wrapDouble(double value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getValueWrapperManager().doubleWrapper(value);
    }

    @Specialization
    protected ValueWrapper wrapBoolean(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return value ? context.getValueWrapperManager().trueWrapper : context.getValueWrapperManager().falseWrapper;
    }

    @Specialization
    protected ValueWrapper wrapUndef(NotProvided value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getValueWrapperManager().undefWrapper;
    }

    @Specialization
    protected ValueWrapper wrapWrappedValue(ValueWrapper value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().argumentError(
                        RopeOperations.encodeAscii("Wrapping wrapped object", UTF8Encoding.INSTANCE),
                        Encodings.UTF_8,
                        this));
    }

    @Specialization
    protected ValueWrapper wrapNil(Nil value) {
        return value.getValueWrapper();
    }

    @Specialization(guards = "!isNil(value)")
    protected ValueWrapper wrapImmutable(ImmutableRubyObject value,
            @Cached BranchProfile noHandleProfile) {
        ValueWrapper wrapper = value.getValueWrapper();
        if (wrapper == null) {
            noHandleProfile.enter();
            synchronized (value) {
                wrapper = value.getValueWrapper();
                if (wrapper == null) {
                    /* This is double-checked locking, but it's safe because the object that we create, the
                     * ValueWrapper, is not published until after a memory store fence. */
                    wrapper = new ValueWrapper(value, UNSET_HANDLE, null);
                    MemoryFence.storeStore();
                    value.setValueWrapper(wrapper);
                }
            }
        }
        return wrapper;
    }

    @Specialization(limit = "getDynamicObjectCacheLimit()")
    protected ValueWrapper wrapValue(RubyDynamicObject value,
            @CachedLibrary("value") DynamicObjectLibrary objectLibrary,
            @Cached BranchProfile noHandleProfile) {
        ValueWrapper wrapper = (ValueWrapper) objectLibrary.getOrDefault(value, Layouts.VALUE_WRAPPER_IDENTIFIER, null);
        if (wrapper == null) {
            noHandleProfile.enter();
            synchronized (value) {
                wrapper = (ValueWrapper) objectLibrary.getOrDefault(value, Layouts.VALUE_WRAPPER_IDENTIFIER, null);
                if (wrapper == null) {
                    /* This is double-checked locking, but it's safe because the object that we create, the
                     * ValueWrapper, is not published until after a memory store fence. */
                    wrapper = new ValueWrapper(value, UNSET_HANDLE, null);
                    MemoryFence.storeStore();
                    objectLibrary.put(value, Layouts.VALUE_WRAPPER_IDENTIFIER, wrapper);
                }
            }
        }
        return wrapper;
    }

    @Specialization(guards = "isForeignObject(value)")
    protected ValueWrapper wrapNonRubyObject(Object value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().argumentError("Attempt to wrap something that isn't an Ruby object", this));
    }

    protected int getDynamicObjectCacheLimit() {
        return RubyLanguage.getCurrentLanguage().options.INSTANCE_VARIABLE_CACHE;
    }
}
