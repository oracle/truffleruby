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
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@GenerateUncached
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ProcOrNullNode extends RubyBaseNode {

    public static ProcOrNullNode create() {
        return ProcOrNullNodeGen.create(null);
    }

    public abstract RubyProc executeProcOrNull(VirtualFrame frame);

    public abstract RubyProc executeProcOrNull(Object proc);

    @Specialization
    protected RubyProc doNotProvided(NotProvided proc) {
        return null;
    }

    @Specialization
    protected RubyProc doNil(Nil nil) {
        return null;
    }

    @Specialization
    protected RubyProc doProc(RubyProc proc) {
        return proc;
    }
}
