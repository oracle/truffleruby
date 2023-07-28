/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@GenerateCached(false)
@GenerateInline
public abstract class PropagateSharingNode extends RubyBaseNode {

    public abstract void execute(Node node, RubyDynamicObject source, Object value);

    @Specialization(guards = "!isSharedNode.executeIsShared(node, source)")
    protected static void propagateNotShared(Node node, RubyDynamicObject source, Object value,
            @Cached @Shared IsSharedNode isSharedNode) {
        // do nothing
    }

    @Specialization(guards = "isSharedNode.executeIsShared(node, source)")
    protected static void propagateShared(Node node, RubyDynamicObject source, Object value,
            @Cached @Shared IsSharedNode isSharedNode,
            @Cached WriteBarrierNode writeBarrierNode) {
        writeBarrierNode.execute(node, value);
    }
}
