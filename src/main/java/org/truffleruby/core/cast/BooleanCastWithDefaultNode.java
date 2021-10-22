/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/** Casts a value into a boolean and defaults to the given value if not provided. */
@NodeChild(value = "value", type = RubyBaseNodeWithExecute.class)
public abstract class BooleanCastWithDefaultNode extends RubyBaseNodeWithExecute {

    public static BooleanCastWithDefaultNode create(boolean defaultValue, RubyBaseNodeWithExecute node) {
        return BooleanCastWithDefaultNodeGen.create(defaultValue, node);
    }

    private final boolean defaultValue;

    public BooleanCastWithDefaultNode(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Specialization
    protected boolean doDefault(NotProvided value) {
        return defaultValue;
    }

    @Specialization
    protected boolean doBoolean(boolean value) {
        return value;
    }

    @Specialization
    protected boolean doNil(Nil nil) {
        return false;
    }

    @Specialization
    protected boolean doSymbol(RubySymbol symbol) {
        return true;
    }

    @Specialization
    protected boolean doInt(int value) {
        return true;
    }

    @Specialization
    protected boolean doLong(long value) {
        return true;
    }

    @Specialization
    protected boolean doFloat(double value) {
        return true;
    }

    @Specialization
    protected boolean doBasicObject(RubyDynamicObject object) {
        return true;
    }

}
