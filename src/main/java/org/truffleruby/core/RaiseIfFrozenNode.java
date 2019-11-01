/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;

@NodeChild("value")
public abstract class RaiseIfFrozenNode extends RubyNode {

    public static RaiseIfFrozenNode create() {
        return RaiseIfFrozenNodeGen.create(null);
    }

    public abstract void execute(Object object);

    @Specialization
    protected Object check(Object value,
            @Cached IsFrozenNode isFrozenNode,
            @Cached BranchProfile errorProfile) {

        if (isFrozenNode.execute(value)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().frozenError(value, this));
        }

        return value;
    }
}
