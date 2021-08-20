/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;

import org.truffleruby.RubyContext;
import org.truffleruby.language.FrameAndVariablesSendingNode;
import org.truffleruby.language.RubyNode;

public interface CallerDataReadingNode {
    public void startSending(FrameAndVariablesSendingNode node);

    public static boolean notifyCallerToSendData(RubyContext context, Node callerNode, CallerDataReadingNode reader) {
        if (callerNode instanceof DirectCallNode || callerNode instanceof IndirectCallNode) {
            Node parent = callerNode.getParent();
            while (parent != null) {
                if (parent instanceof FrameAndVariablesSendingNode) {
                    reader.startSending((FrameAndVariablesSendingNode) parent);
                    return true;
                }
                if (parent instanceof RubyNode) {
                    // A node with source info representing Ruby code, we could not find the FrameAndVariablesSendingNode
                    return false;
                }
                parent = parent.getParent();
            }
        }

        return false;
    }
}
