/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

public abstract class IsFrozenNode extends RubyBaseNode {

    private final BranchProfile errorProfile = BranchProfile.create();

    public static IsFrozenNode create() {
        return IsFrozenNodeGen.create();
    }

    public abstract boolean executeIsFrozen(Object object);

    public void raiseIfFrozen(Object object) {
        if (executeIsFrozen(object)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().frozenError(object, this));
        }
    }

    @Specialization
    protected boolean isFrozen(boolean object) {
        return true;
    }

    @Specialization
    protected boolean isFrozen(int object) {
        return true;
    }

    @Specialization
    protected boolean isFrozen(long object) {
        return true;
    }

    @Specialization
    protected boolean isFrozen(double object) {
        return true;
    }

    @Specialization
    protected boolean isFrozen(
            DynamicObject object,
            @Cached ReadObjectFieldNode readFrozenNode) {
        return (boolean) readFrozenNode.execute(object, Layouts.FROZEN_IDENTIFIER, false);
    }

    @TruffleBoundary
    public static boolean isFrozen(Object object) {
        return !(object instanceof DynamicObject) ||
                (boolean) ((DynamicObject) object).get(Layouts.FROZEN_IDENTIFIER, false);
    }

}
