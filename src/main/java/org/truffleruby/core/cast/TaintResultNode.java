/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.cast;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class TaintResultNode extends RubyContextSourceNode {

    private final boolean taintFromSelf;
    private final int taintFromParameter;
    private final BranchProfile doNotTaintProfile = BranchProfile.create();
    private final ConditionProfile taintProfile = ConditionProfile.create();

    @Child private RubyNode method;
    @Child private RubyLibrary rubyLibrarySource;
    @Child private RubyLibrary rubyLibraryResult;

    public TaintResultNode(boolean taintFromSelf, int taintFromParameter, RubyNode method) {
        this.taintFromSelf = taintFromSelf;
        this.taintFromParameter = taintFromParameter;
        this.method = method;
    }

    public TaintResultNode() {
        this(false, -1, null);
    }

    public Object maybeTaint(Object source, Object result) {
        if (taintProfile.profile(isTainted(source))) {
            if (rubyLibraryResult == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                rubyLibraryResult = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
            }
            rubyLibraryResult.taint(result);
        }

        return result;
    }

    private boolean isTainted(Object result) {
        if (rubyLibrarySource == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            rubyLibrarySource = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
        }
        return rubyLibrarySource.isTainted(result);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object result;

        try {
            result = method.execute(frame);
        } catch (DoNotTaint e) {
            doNotTaintProfile.enter();
            return e.getResult();
        }

        if (result != nil) {
            if (taintFromSelf) {
                maybeTaint(RubyArguments.getSelf(frame), result);
            }

            if (taintFromParameter != -1) {
                // It's possible the taintFromParameter value was misconfigured by the user, but the far more likely
                // scenario is that the argument at that position is a NotProvided argument, which doesn't take up
                // a space in the frame.
                if (taintFromParameter < RubyArguments.getArgumentsCount(frame)) {
                    final Object argument = RubyArguments.getArgument(frame, taintFromParameter);

                    if (argument instanceof RubyDynamicObject) {
                        final RubyDynamicObject taintSource = (RubyDynamicObject) argument;
                        maybeTaint(taintSource, result);
                    }
                }
            }
        }

        return result;
    }

    public static class DoNotTaint extends ControlFlowException {
        private static final long serialVersionUID = 5321304910918469059L;

        private final Object result;

        public DoNotTaint(Object result) {
            this.result = result;
        }

        public Object getResult() {
            return result;
        }
    }
}
