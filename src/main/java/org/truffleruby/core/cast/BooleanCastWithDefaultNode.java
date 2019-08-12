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

import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Casts a value into a boolean and defaults to the given value if not provided.
 */
@NodeChild(value = "value", type = RubyNode.class)
public abstract class BooleanCastWithDefaultNode extends RubyNode {

    private final boolean defaultValue;

    public BooleanCastWithDefaultNode(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Specialization
    public boolean doDefault(NotProvided value) {
        return defaultValue;
    }

    @Specialization
    public boolean doBoolean(boolean value) {
        return value;
    }

    @Specialization(guards = "isNil(nil)")
    public boolean doNil(Object nil) {
        return false;
    }

    @Specialization
    public boolean doInt(int value) {
        return true;
    }

    @Specialization
    public boolean doLong(long value) {
        return true;
    }

    @Specialization
    public boolean doFloat(double value) {
        return true;
    }

    @Specialization(guards = "!isNil(object)")
    public boolean doBasicObject(DynamicObject object) {
        return true;
    }

}
