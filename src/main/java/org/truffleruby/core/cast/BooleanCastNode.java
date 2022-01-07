/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** Casts a value into a boolean. */
@GenerateUncached
@NodeChild(value = "value", type = RubyNode.class)
public abstract class BooleanCastNode extends RubyBaseNode implements BooleanExecute {

    public static BooleanCastNode create() {
        return BooleanCastNodeGen.create(null);
    }

    public static BooleanExecute createIfNeeded(RubyNode possibleBoolean) {
        if (possibleBoolean.needsBooleanCastNode()) {
            return BooleanCastNodeGen.create(possibleBoolean);
        }

        BooleanExecute node = (BooleanExecute) possibleBoolean;
        node.markAvoidedCast();
        return node;
    }

    public abstract RubyNode getValue();

    /** Execute with child node */
    public abstract boolean executeBoolean(VirtualFrame frame);

    /** Execute with given value */
    public abstract boolean executeToBoolean(Object value);

    @Specialization
    protected boolean doNil(Nil nil) {
        return false;
    }

    @Specialization
    protected boolean doBoolean(boolean value) {
        return value;
    }

    @Specialization
    protected boolean doInt(int value) {
        return true;
    }

    @Specialization
    protected boolean doLong(long value) {
        return true;
    }

    @Specialization
    protected boolean doFloat(double value) {
        return true;
    }

    @Specialization
    protected boolean doBasicObject(RubyDynamicObject object) {
        return true;
    }

    @Specialization(guards = "!isNil(object)")
    protected boolean doImmutableObject(ImmutableRubyObject object) {
        return true;
    }

    @Specialization(guards = "isForeignObject(object)", limit = "getCacheLimit()")
    protected boolean doForeignObject(Object object,
            @CachedLibrary("object") InteropLibrary objects,
            @Cached ConditionProfile isNullProfile,
            @Cached ConditionProfile isBooleanProfile,
            @Cached BranchProfile failed) {

        if (isNullProfile.profile(objects.isNull(object))) {
            return false;
        } else {
            if (isBooleanProfile.profile(objects.isBoolean(object))) {
                try {
                    return objects.asBoolean(object);
                } catch (UnsupportedMessageException e) {
                    failed.enter();
                    // it concurrently stopped being boolean
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    protected int getCacheLimit() {
        return getLanguage().options.METHOD_LOOKUP_CACHE;
    }

    @Override
    public void markAvoidedCast() {
    }

    @Override
    public boolean didAvoidCast() {
        return false;
    }
}
