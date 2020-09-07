/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

import static org.truffleruby.language.dispatch.DispatchNode.PUBLIC;

public class SymbolProcNode extends RubyContextSourceNode {

    private final String symbol;
    private final BranchProfile noReceiverProfile = BranchProfile.create();

    @Child private DispatchNode callNode;

    public SymbolProcNode(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        // Not using CheckArityNode as the message is different and arity is reported as -1
        final int given = RubyArguments.getArgumentsCount(frame);
        if (given == 0) {
            noReceiverProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().argumentError("no receiver given", this));
        }

        final Object receiver = RubyArguments.getArgument(frame, 0);
        final Object[] arguments = ArrayUtils.extractRange(RubyArguments.getArguments(frame), 1, given);
        final RubyProc block = RubyArguments.getBlock(frame);

        return getCallNode().dispatch(frame, receiver, symbol, block, arguments);
    }

    private DispatchNode getCallNode() {
        if (callNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callNode = insert(DispatchNode.create(PUBLIC));
        }

        return callNode;
    }

}
