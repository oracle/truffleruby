/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.DynamicReturnException;
import org.truffleruby.language.control.LocalReturnException;
import org.truffleruby.language.control.NextException;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.control.RetryException;

public class EvalCatchNode extends RubyContextSourceNode {

    @Child private RubyNode body;

    private final boolean catchReturn;
    private final boolean catchRetry;

    private final BranchProfile returnProfile = BranchProfile.create();
    private final BranchProfile retryProfile = BranchProfile.create();
    private final BranchProfile nextProfile = BranchProfile.create();

    public EvalCatchNode(RubyNode body, boolean catchReturn, boolean catchRetry) {
        this.body = body;
        this.catchReturn = catchReturn;
        this.catchRetry = catchRetry;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            return body.execute(frame);
        } catch (LocalReturnException | DynamicReturnException e) {
            returnProfile.enter();
            if (catchReturn) {
                throw new RaiseException(getContext(), coreExceptions().unexpectedReturn(this));
            } else {
                throw e;
            }
        } catch (RetryException e) {
            retryProfile.enter();
            if (catchRetry) {
                throw new RaiseException(getContext(), coreExceptions().syntaxErrorInvalidRetry(this));
            } else {
                throw e;
            }
        } catch (NextException e) {
            nextProfile.enter();
            return e.getResult();
        }
    }

}
