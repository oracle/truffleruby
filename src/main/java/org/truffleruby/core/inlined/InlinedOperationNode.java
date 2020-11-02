/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

public abstract class InlinedOperationNode extends InlinedReplaceableNode {

    public InlinedOperationNode(
            RubyLanguage language,
            RubyCallNodeParameters callNodeParameters,
            Assumption... assumptions) {
        super(language, callNodeParameters, assumptions);
    }

    protected Object rewriteAndCall(VirtualFrame frame, Object receiver, Object... arguments) {
        return rewriteAndCallWithBlock(frame, receiver, null, arguments);
    }

    protected Object rewriteAndCallWithBlock(VirtualFrame frame, Object receiver, RubyProc block,
            Object... arguments) {
        return rewriteToCallNode().executeWithArgumentsEvaluated(frame, receiver, block, arguments);
    }

    protected CoreMethods coreMethods() {
        return getContext().getCoreMethods();
    }
}
