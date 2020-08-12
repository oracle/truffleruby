/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

/** Casts an object to a Ruby Proc object. */
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToProcNode extends RubyContextSourceNode {

    @Specialization
    protected Nil doNil(Nil nil) {
        return nil;
    }

    @Specialization
    protected RubyProc doRubyProc(RubyProc proc) {
        return proc;
    }

    @Specialization(guards = { "!isNil(object)", "!isRubyProc(object)" })
    protected RubyProc doObject(VirtualFrame frame, Object object,
            @Cached(parameters = "PRIVATE") CallDispatchHeadNode toProc,
            @Cached BranchProfile errorProfile) {
        final Object coerced;
        try {
            coerced = toProc.dispatch(frame, object, "to_proc", null, EMPTY_ARGUMENTS);
        } catch (RaiseException e) {
            errorProfile.enter();
            if (e.getException().getLogicalClass() == coreLibrary().noMethodErrorClass) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorNoImplicitConversion(object, "Proc", this));
            } else {
                throw e;
            }
        }

        if (coerced instanceof RubyProc) {
            return (RubyProc) coerced;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorBadCoercion(object, "Proc", "to_proc", coerced, this));
        }
    }

}
