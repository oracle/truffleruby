/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.NotProvided;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/** Casts a value into a boolean and defaults to the given value if not provided. */
@NodeChild(value = "valueNode", type = RubyBaseNodeWithExecute.class)
public abstract class BooleanCastWithDefaultNode extends RubyBaseNodeWithExecute {

    public static BooleanCastWithDefaultNode create(boolean defaultValue, RubyBaseNodeWithExecute node) {
        return BooleanCastWithDefaultNodeGen.create(defaultValue, node);
    }

    private final boolean defaultValue;

    public BooleanCastWithDefaultNode(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public abstract RubyBaseNodeWithExecute getValueNode();

    @Specialization
    protected boolean doDefault(NotProvided value) {
        return defaultValue;
    }

    @Fallback
    protected boolean fallback(Object value,
            @Cached BooleanCastNode booleanCastNode) {
        return booleanCastNode.execute(value);
    }

    @Override
    public RubyBaseNodeWithExecute cloneUninitialized() {
        return create(defaultValue, getValueNode().cloneUninitialized());
    }
}
