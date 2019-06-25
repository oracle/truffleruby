/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyNode;

@GenerateUncached
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ProcOrNullNode extends RubyBaseWithoutContextNode {

    public static ProcOrNullNode create() {
        return ProcOrNullNodeGen.create(null);
    }

    public abstract DynamicObject executeProcOrNull(VirtualFrame frame);

    public abstract DynamicObject executeProcOrNull(Object proc);

    @Specialization
    public DynamicObject doNotProvided(NotProvided proc) {
        return null;
    }

    @Specialization(guards = "isNil(context, nil)")
    public DynamicObject doNil(Object nil,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return null;
    }

    @Specialization(guards = "isRubyProc(proc)")
    public DynamicObject doProc(DynamicObject proc) {
        return proc;
    }

}
