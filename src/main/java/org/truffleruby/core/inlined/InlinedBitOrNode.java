/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.core.numeric.FixnumNodes.BitOrNode;
import org.truffleruby.core.numeric.FixnumNodesFactory.BitOrNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedBitOrNode extends BinaryInlinedOperationNode {

    @Child BitOrNode fixnumBitOr;

    public InlinedBitOrNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(callNodeParameters,
                context.getCoreMethods().fixnumBitOrAssumption);
    }

    @Specialization(assumptions = "assumptions")
    Object intBitOr(int a, int b) {
        return getBitOrNode().executeBitOr(a, b);
    }

    @Specialization(assumptions = "assumptions")
    Object longBitOr(long a, long b) {
        return getBitOrNode().executeBitOr(a, b);
    }

    @Specialization
    Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    private BitOrNode getBitOrNode() {
        if (fixnumBitOr == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fixnumBitOr = insert(BitOrNodeFactory.create(null));
        }
        return fixnumBitOr;
    }

}
