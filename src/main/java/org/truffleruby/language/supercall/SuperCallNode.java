/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.cast.ProcOrNullNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

public class SuperCallNode extends RubyNode {

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
        final DynamicObject blockObject = procOrNullNode.executeProcOrNull(block.execute(frame));

        final InternalMethod superMethod = executeLookupSuperMethod(frame, self);

        return callSuperMethodNode.executeCallSuperMethod(frame, self, superMethod, superArguments, blockObject);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        final Object self = RubyArguments.getSelf(frame);
        final InternalMethod superMethod = executeLookupSuperMethod(frame, self);

        if (superMethod == null) {
            return nil();
        } else {
            return coreStrings().SUPER.createInstance();
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
            procOrNullNode = insert(ProcOrNullNodeGen.create(null));
        }
        if (callSuperMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callSuperMethodNode = insert(CallSuperMethodNode.create());
        }
    }

}
