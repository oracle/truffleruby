/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

/**
 * Casts an object to a Ruby Proc object.
 */
@NodeChild("child")
public abstract class ToProcNode extends RubyNode {

    @Specialization(guards = "isNil(nil)")
    protected DynamicObject doNil(Object nil) {
        return nil();
    }

    @Specialization(guards = "isRubyProc(proc)")
    protected DynamicObject doRubyProc(DynamicObject proc) {
        return proc;
    }

    @Specialization(guards = "!isRubyProc(object)")
    protected DynamicObject doObject(VirtualFrame frame, Object object,
            @Cached("createPrivate()") CallDispatchHeadNode toProc,
            @Cached BranchProfile errorProfile) {
        final Object coerced;
        try {
            coerced = toProc.dispatch(frame, object, "to_proc", null, EMPTY_ARGUMENTS);
        } catch (RaiseException e) {
            errorProfile.enter();
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == coreLibrary().getNoMethodErrorClass()) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorNoImplicitConversion(object, "Proc", this));
            } else {
                throw e;
            }
        }

        if (RubyGuards.isRubyProc(coerced)) {
            return (DynamicObject) coerced;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorBadCoercion(object, "Proc", "to_proc", coerced, this));
        }
    }

}
