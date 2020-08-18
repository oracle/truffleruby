/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.objects.IsANode;

public abstract class InlinedKindOfNode extends BinaryInlinedOperationNode {

    protected static final String METHOD = "kind_of?";

    public InlinedKindOfNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(context, callNodeParameters);
    }

    @Specialization(
            guards = {
                    "!isForeignObject(self)",
                    "lookupNode.lookup(frame, self, METHOD) == coreMethods().KERNEL_KIND_OF",
            },
            limit = "1")
    protected boolean doKindOf(VirtualFrame frame, Object self, RubyModule module,
            @Cached LookupMethodNode lookupNode,
            @Cached IsANode isANode) {
        return isANode.executeIsA(self, module);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

}
