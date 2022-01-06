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

import com.oracle.truffle.api.dsl.Cached;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;
import org.truffleruby.language.objects.IsANode;

public abstract class InlinedIsANode extends BinaryInlinedBooleanNode {

    protected static final String METHOD = "is_a?";

    public InlinedIsANode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
    }

    @Specialization(
            guards = "lookupNode.lookupProtected(frame, self, METHOD) == coreMethods().KERNEL_IS_A",
            assumptions = "assumptions",
            limit = "1")
    protected boolean doIsA(VirtualFrame frame, Object self, RubyModule module,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached IsANode isANode) {
        return isANode.executeIsA(self, module);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

}
