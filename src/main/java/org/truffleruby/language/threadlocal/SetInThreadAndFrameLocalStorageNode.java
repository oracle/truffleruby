/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.threadlocal;

import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.parser.ReadLocalNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class SetInThreadAndFrameLocalStorageNode extends RubyNode {

    @Child private RubyNode variableNode;
    @Child private RubyNode writeNode;
    @Child private RubyNode valueNode;
    private final ConditionProfile isStorageProfile = ConditionProfile.createBinaryProfile();

    public SetInThreadAndFrameLocalStorageNode(ReadLocalNode variable, RubyNode value) {
        this.variableNode = variable;
        writeNode = variable.makeWriteNode(new NewThreadAndFrameLocalStorageNode());
        this.valueNode = value;
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return variableNode.isDefined(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object variableObject = variableNode.execute(frame);
        final ThreadAndFrameLocalStorage storage;
        if (isStorageProfile.profile(RubyGuards.isThreadLocal(variableObject))) {
            storage = (ThreadAndFrameLocalStorage) variableObject;
        } else {
            storage = (ThreadAndFrameLocalStorage) writeNode.execute(frame);
        }

        Object result = valueNode.execute(frame);
        storage.set(result);
        return result;
    }
}
