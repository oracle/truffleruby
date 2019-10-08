/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.IsFrozenNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class RaiseIfFrozenNode extends RubyNode {

    private final BranchProfile errorProfile = BranchProfile.create();

    @Child private RubyNode child;
    @Child private IsFrozenNode isFrozenNode;

    public RaiseIfFrozenNode(RubyNode child) {
        this.child = child;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object result = child.execute(frame);

        if (executeIsFrozen(result)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().frozenError(result, this));
        }

        return result;
    }

    private boolean executeIsFrozen(Object value) {
        if (isFrozenNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isFrozenNode = insert(IsFrozenNode.create());
        }
        return isFrozenNode.executeIsFrozen(value);
    }
}
