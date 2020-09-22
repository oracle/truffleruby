/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class WriteInstanceVariableNode extends RubyContextSourceNode {

    private final String name;

    @Child private RubyNode receiver;
    @Child private RubyNode rhs;
    @Child private WriteObjectFieldNode writeNode;

    private final ConditionProfile objectProfile = ConditionProfile.create();

    public WriteInstanceVariableNode(String name, RubyNode receiver, RubyNode rhs) {
        this.name = name;
        this.receiver = receiver;
        this.rhs = rhs;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object object = receiver.execute(frame);
        final Object value = rhs.execute(frame);

        if (objectProfile.profile(object instanceof RubyDynamicObject)) {
            if (writeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                writeNode = insert(WriteObjectFieldNode.create());
            }

            writeNode.execute((RubyDynamicObject) object, name, value);
        } else {
            throw new RaiseException(getContext(), coreExceptions().frozenError(object, this));
        }

        return value;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        return coreStrings().ASSIGNMENT.createInstance(context);
    }

}
