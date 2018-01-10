/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;

@NodeChild(value = "value", type = RubyNode.class)
public abstract class HasKeysNode extends RubyNode {

    public static HasKeysNode create() {
        return HasKeysNodeGen.create(null);
    }

    public abstract boolean executeHasKeys(Object value);

    @Specialization(guards = "isNil(value)")
    public boolean hasKeysNil(DynamicObject value) {
        return false;
    }

    @Specialization
    public boolean hasKeys(boolean value) {
        return false;
    }

    @Specialization
    public boolean hasKeys(byte value) {
        return false;
    }

    @Specialization
    public boolean hasKeys(short value) {
        return false;
    }

    @Specialization
    public boolean hasKeys(int value) {
        return false;
    }

    @Specialization
    public boolean hasKeys(long value) {
        return false;
    }

    @Specialization
    public boolean hasKeys(float value) {
        return false;
    }

    @Specialization
    public boolean hasKeys(double value) {
        return false;
    }

    @Specialization(guards = "isRubyBignum(value)")
    public boolean hasKeysBignum(DynamicObject value) {
        return false;
    }

    @Specialization(guards = "isRubySymbol(value)")
    public boolean hasKeysSymbol(DynamicObject value) {
        return false;
    }

    @Fallback
    public boolean hasKeysFallback(Object value) {
        return true;
    }

}
