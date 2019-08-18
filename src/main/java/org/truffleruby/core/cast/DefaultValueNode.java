/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/**
 * Return the given default value if the argument is not provided.
 */
@NodeChild(value = "value", type = RubyNode.class)
public abstract class DefaultValueNode extends RubyNode {

    private final Object defaultValue;

    public DefaultValueNode(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Specialization
    protected Object doDefault(NotProvided value) {
        return defaultValue;
    }

    @Specialization(guards = "wasProvided(value)")
    protected Object doProvided(Object value) {
        return value;
    }

}
