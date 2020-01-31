/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class CheckArityNode extends RubyContextSourceNode {

    private final Arity arity;
    @Child private RubyNode body;

    private final BranchProfile checkFailedProfile = BranchProfile.create();

    public CheckArityNode(Arity arity, RubyNode body) {
        this.arity = arity;
        this.body = body;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        checkArity(frame);
        body.doExecuteVoid(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        checkArity(frame);
        return body.execute(frame);
    }

    private void checkArity(VirtualFrame frame) {
        final int given = RubyArguments.getArgumentsCount(frame);

        if (!checkArity(arity, given)) {
            checkFailedProfile.enter();
            if (arity.hasRest()) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentErrorPlus(given, arity.getRequired(), this));
            } else if (arity.getOptional() > 0) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(given, arity.getRequired(), arity.getOptional(), this));
            } else {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(given, arity.getRequired(), this));
            }
        }
    }

    static boolean checkArity(Arity arity, int given) {
        final int required = arity.getRequired();

        if (required != 0 && given < required) {
            return false;
        }

        if (!arity.hasRest() && given > required + arity.getOptional()) {
            return false;
        }

        return true;
    }

}
