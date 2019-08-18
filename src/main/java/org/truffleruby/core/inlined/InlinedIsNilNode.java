/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedIsNilNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "nil?";

    public InlinedIsNilNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(callNodeParameters,
                context.getCoreMethods().nilClassIsNilAssumption);
    }

    @Specialization(guards = "isNil(self)", assumptions = "assumptions")
    protected boolean nil(Object self) {
        return true;
    }

    @Specialization(guards = {
            "!isForeignObject(self)",
            "lookupNode.lookup(frame, self, METHOD) == coreMethods().KERNEL_IS_NIL",
    }, assumptions = "assumptions", limit = "1")
    protected boolean notNil(VirtualFrame frame, Object self,
            @Cached LookupMethodNode lookupNode) {
        return false;
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }

}
