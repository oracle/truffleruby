/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class PropagateSharingNode extends RubyBaseNode {

    public static PropagateSharingNode create() {
        return PropagateSharingNodeGen.create();
    }

    public abstract void executePropagate(RubyDynamicObject source, Object value);

    @Specialization(guards = "!isSharedNode.executeIsShared(source)", limit = "1")
    protected void propagateNotShared(RubyDynamicObject source, Object value,
            @Cached @Shared("isSharedNode") IsSharedNode isSharedNode) {
        // do nothing
    }

    @Specialization(guards = "isSharedNode.executeIsShared(source)", limit = "1")
    protected void propagateShared(RubyDynamicObject source, Object value,
            @Cached @Shared("isSharedNode") IsSharedNode isSharedNode,
            @Cached WriteBarrierNode writeBarrierNode) {
        writeBarrierNode.executeWriteBarrier(value);
    }
}
