/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import org.truffleruby.RubyContext;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@GenerateWrapper
public abstract class RescueNode extends RubyContextSourceNode {

    @Child private RubyNode rescueBody;

    @Child private CallDispatchHeadNode callTripleEqualsNode;
    @Child private BooleanCastNode booleanCastNode;

    private final BranchProfile errorProfile = BranchProfile.create();

    public RescueNode(RubyNode rescueBody) {
        this.rescueBody = rescueBody;
    }

    // Constructor for instrumentation
    protected RescueNode() {
        this.rescueBody = null;
    }

    public abstract boolean canHandle(VirtualFrame frame, DynamicObject exception);

    @Override
    public Object execute(VirtualFrame frame) {
        return rescueBody.execute(frame);
    }

    protected boolean matches(VirtualFrame frame, Object exception, Object handlingClass) {
        if (!(handlingClass instanceof RubyModule)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorRescueInvalidClause(this));
        }

        if (callTripleEqualsNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callTripleEqualsNode = insert(CallDispatchHeadNode.createPrivate());
        }
        if (booleanCastNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            booleanCastNode = insert(BooleanCastNodeGen.create(null));
        }

        final Object matches = callTripleEqualsNode.call(handlingClass, "===", exception);
        return booleanCastNode.executeToBoolean(matches);
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probe) {
        return new RescueNodeWrapper(this, probe);
    }

    // Declared abstract here so the instrumentation wrapper delegates it
    @Override
    public abstract Object isDefined(VirtualFrame frame, RubyContext context);

}
