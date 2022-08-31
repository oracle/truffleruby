/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild(value = "childNode", type = RubyBaseNodeWithExecute.class)
public abstract class ToPathNode extends RubyBaseNodeWithExecute {

    public static ToPathNode create(RubyBaseNodeWithExecute child) {
        return ToPathNodeGen.create(child);
    }

    public abstract RubyBaseNodeWithExecute getChildNode();

    @Specialization
    protected RubyString coerceRubyString(RubyString path) {
        return path;
    }

    @Specialization
    protected ImmutableRubyString coerceImmutableRubyString(ImmutableRubyString path) {
        return path;
    }

    @Specialization(guards = "isNotRubyString(object)")
    protected RubyString coerceObject(Object object,
            @Cached DispatchNode toPathNode) {
        return (RubyString) toPathNode.call(coreLibrary().truffleTypeModule, "coerce_to_path", object);
    }

    @Override
    public RubyBaseNodeWithExecute cloneUninitialized() {
        return create(getChildNode().cloneUninitialized());
    }
}
