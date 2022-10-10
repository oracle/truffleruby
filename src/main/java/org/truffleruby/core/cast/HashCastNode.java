/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

/** Must be a RubyNode because it's used for ** in the translator. */
@NodeChild(value = "childNode", type = RubyNode.class)
public abstract class HashCastNode extends RubyContextSourceNode {

    public static HashCastNode create() {
        return HashCastNodeGen.create(null);
    }

    public static HashCastNode create(RubyNode child) {
        return HashCastNodeGen.create(child);
    }

    public abstract RubyHash execute(Object value);

    protected abstract RubyNode getChildNode();

    @Specialization
    protected RubyHash castHash(RubyHash hash) {
        return hash;
    }

    @Specialization(guards = "!isRubyHash(object)")
    protected RubyHash cast(Object object,
            @Cached BranchProfile errorProfile,
            @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode toHashNode) {
        final Object result = toHashNode.call(object, "to_hash");

        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorNoImplicitConversion(object, "Hash", this));
        }

        if (!RubyGuards.isRubyHash(result)) {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorCantConvertTo(object, "Hash", "to_hash", result, this));
        }

        return (RubyHash) result;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        getChildNode().doExecuteVoid(frame);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = create(getChildNode().cloneUninitialized());
        return copy.copyFlags(this);
    }

}
