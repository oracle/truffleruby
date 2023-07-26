/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.core.cast.HashCastNodeGen.HashCastASTNodeGen;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE_RETURN_MISSING;

public abstract class HashCastNode extends RubyBaseNode {

    public abstract RubyHash execute(Object value);

    @Specialization
    protected RubyHash castHash(RubyHash hash) {
        return hash;
    }

    @Specialization(guards = "!isRubyHash(object)")
    protected RubyHash cast(Object object,
            @Cached InlinedBranchProfile errorProfile,
            @Cached DispatchNode toHashNode) {
        final Object result = toHashNode.call(PRIVATE_RETURN_MISSING, object, "to_hash");

        if (result == DispatchNode.MISSING) {
            errorProfile.enter(this);
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorNoImplicitConversion(object, "Hash", this));
        }

        if (!RubyGuards.isRubyHash(result)) {
            errorProfile.enter(this);
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorCantConvertTo(object, "Hash", "to_hash", result, this));
        }

        return (RubyHash) result;
    }

    /** Must be a RubyNode because it's used for ** in the translator. */
    @NodeChild(value = "childNode", type = RubyNode.class)
    public abstract static class HashCastASTNode extends RubyContextSourceNode {

        protected abstract RubyNode getChildNode();

        @Specialization
        protected RubyHash cast(Object object,
                @Cached HashCastNode hashCastNode) {
            return hashCastNode.execute(object);

        }

        @Override
        public void doExecuteVoid(VirtualFrame frame) {
            getChildNode().doExecuteVoid(frame);
        }

        @Override
        public RubyNode cloneUninitialized() {
            var copy = HashCastASTNodeGen.create(getChildNode().cloneUninitialized());
            return copy.copyFlags(this);
        }
    }
}
