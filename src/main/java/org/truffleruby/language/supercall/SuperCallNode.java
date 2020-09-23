/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import org.truffleruby.RubyContext;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public class SuperCallNode extends RubyContextSourceNode {

    @Child private RubyNode arguments;
    @Child private RubyNode block;
    @Child private ProcOrNullNode procOrNullNode;
    @Child private LookupSuperMethodNode lookupSuperMethodNode;
    @Child private CallSuperMethodNode callSuperMethodNode;

    public SuperCallNode(RubyNode arguments, RubyNode block) {
        this.arguments = arguments;
        this.block = block;
    }

    @Override
    public final Object execute(VirtualFrame frame) {
        initNodes();
        final Object self = RubyArguments.getSelf(frame);

        // Execute the arguments
        final Object[] superArguments = (Object[]) arguments.execute(frame);

        // Execute the block
        final RubyProc blockObject = procOrNullNode.executeProcOrNull(block.execute(frame));

        final InternalMethod superMethod = executeLookupSuperMethod(frame, self);

        return callSuperMethodNode.execute(frame, self, superMethod, superArguments, blockObject);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        final Object self = RubyArguments.getSelf(frame);
        final InternalMethod superMethod = executeLookupSuperMethod(frame, self);

        if (superMethod == null) {
            return nil;
        } else {
            return coreStrings().SUPER.createInstance(context);
        }
    }

    private InternalMethod executeLookupSuperMethod(VirtualFrame frame, Object self) {
        if (lookupSuperMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupSuperMethodNode = insert(LookupSuperMethodNodeGen.create());
        }
        return lookupSuperMethodNode.executeLookupSuperMethod(frame, self);
    }

    private void initNodes() {
        if (procOrNullNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            procOrNullNode = insert(ProcOrNullNode.create());
        }
        if (callSuperMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callSuperMethodNode = insert(CallSuperMethodNode.create());
        }
    }

}
