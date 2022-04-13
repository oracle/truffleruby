/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.bool;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.cast.BooleanCastNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule(value = "FalseClass", isClass = true)
public abstract class FalseClassNodes {

    @CoreMethod(names = "&", needsSelf = false, required = 1)
    public abstract static class AndNode extends UnaryCoreMethodNode {

        @Specialization
        protected boolean and(Object other) {
            return false;
        }
    }

    @CoreMethod(names = { "|", "^" }, needsSelf = false, required = 1)
    public abstract static class OrXorNode extends UnaryCoreMethodNode {

        @Specialization
        protected boolean orXor(Object other,
                @Cached BooleanCastNode cast) {
            return cast.execute(other);
        }

    }

}
