/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;

@CoreClass("Truffle")
public abstract class TruffleNodes {

    @CoreMethod(names = "graal?", onSingleton = true)
    public abstract static class GraalNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean graal() {
            return getContext().getOptions().GRAAL_PRESENT;
        }

    }

    @CoreMethod(names = "aot?", onSingleton = true)
    public abstract static class AOTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean aot() {
            return TruffleOptions.AOT;
        }

    }

}
