/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE_RETURN_MISSING;

// TODO(CS): copy and paste of ArrayCastNode

@NodeChild(value = "child", type = RubyNode.class)
public abstract class HashCastNode extends RubyContextSourceNode {

    @Child private DispatchNode toHashNode = DispatchNode.create(PRIVATE_RETURN_MISSING);

    protected abstract RubyNode getChild();

    @Specialization
    protected Object cast(boolean value) {
        return nil;
    }

    @Specialization
    protected Object cast(int value) {
        return nil;
    }

    @Specialization
    protected Object cast(long value) {
        return nil;
    }

    @Specialization
    protected Object cast(double value) {
        return nil;
    }

    @Specialization
    protected Object castNil(Nil nil) {
        return nil;
    }

    @Specialization
    protected Object castBignum(RubyBignum value) {
        return nil;
    }

    @Specialization
    protected Object castHash(RubyHash hash) {
        return hash;
    }

    @Specialization(guards = { "!isRubyBignum(object)", "!isRubyHash(object)" })
    protected Object cast(RubyDynamicObject object,
            @Cached BranchProfile errorProfile) {
        final Object result = toHashNode.call(object, "to_hash");

        if (result == DispatchNode.MISSING) {
            return nil;
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
