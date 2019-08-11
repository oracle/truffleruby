/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.bool;

import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.cast.BooleanCastNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

@CoreClass("TrueClass")
public abstract class TrueClassNodes {

    @CoreMethod(names = "&", needsSelf = false, required = 1)
    public abstract static class AndNode extends UnaryCoreMethodNode {

        @Specialization
        public boolean and(Object other,
                @Cached BooleanCastNode cast) {
            return cast.executeToBoolean(other);
        }
    }

    @CoreMethod(names = "|", needsSelf = false, required = 1)
    public abstract static class OrNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean or(Object other) {
            return true;
        }
    }

    @CoreMethod(names = "^", needsSelf = false, required = 1)
    public abstract static class XorNode extends UnaryCoreMethodNode {

        @Specialization
        public boolean xor(Object other,
                @Cached BooleanCastNode cast) {
            return !cast.executeToBoolean(other);
        }
    }

}
