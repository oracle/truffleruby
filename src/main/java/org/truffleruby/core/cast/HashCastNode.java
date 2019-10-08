/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

// TODO(CS): copy and paste of ArrayCastNode

@NodeChild("child")
public abstract class HashCastNode extends RubyNode {

    @Child private CallDispatchHeadNode toHashNode = CallDispatchHeadNode.createReturnMissing();

    protected abstract RubyNode getChild();

    @Specialization
    protected DynamicObject cast(boolean value) {
        return nil();
    }

    @Specialization
    protected DynamicObject cast(int value) {
        return nil();
    }

    @Specialization
    protected DynamicObject cast(long value) {
        return nil();
    }

    @Specialization
    protected DynamicObject cast(double value) {
        return nil();
    }

    @Specialization(guards = "isNil(nil)")
    protected DynamicObject castNil(DynamicObject nil) {
        return nil();
    }

    @Specialization(guards = "isRubyBignum(value)")
    protected DynamicObject castBignum(DynamicObject value) {
        return nil();
    }

    @Specialization(guards = "isRubyHash(hash)")
    protected DynamicObject castHash(DynamicObject hash) {
        return hash;
    }

    @Specialization(guards = { "!isNil(object)", "!isRubyBignum(object)", "!isRubyHash(object)" })
    protected Object cast(VirtualFrame frame, DynamicObject object,
            @Cached BranchProfile errorProfile) {
        final Object result = toHashNode.call(object, "to_hash");

        if (result == DispatchNode.MISSING) {
            return nil();
        }

        if (!RubyGuards.isRubyHash(result)) {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorCantConvertTo(object, "Hash", "to_hash", result, this));
        }

        return result;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        getChild().doExecuteVoid(frame);
    }

}
