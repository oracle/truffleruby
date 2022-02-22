/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.RubyArguments;

public class CallMethodWithDeclaredReceiverNode extends RubyContextSourceNode {

    private final InternalMethod method;

    @Child private CallInternalMethodNode callInternalMethodNode = CallInternalMethodNode.create();

    public CallMethodWithDeclaredReceiverNode(InternalMethod method) {
        this.method = method;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object receiver = RubyArguments.getSelf(RubyArguments.getDeclarationFrame(frame));
        final Object[] repackedArgs = RubyArguments.repack(frame.getArguments(), method, receiver);
        return callInternalMethodNode.execute(frame, method, receiver, repackedArgs);
    }

}
