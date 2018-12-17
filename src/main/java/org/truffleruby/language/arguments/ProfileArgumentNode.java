/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.truffleruby.language.RubyNode;

@NodeInfo(cost = NodeCost.NONE)
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ProfileArgumentNode extends RubyNode {

    protected abstract RubyNode getChild();

    @Specialization(guards = "value == cachedValue", limit = "1")
    protected boolean cacheBoolean(boolean value,
            @Cached("value") boolean cachedValue) {
        return cachedValue;
    }

    @Specialization(guards = "value == cachedValue", limit = "1")
    protected int cacheInt(int value,
            @Cached("value") int cachedValue) {
        return cachedValue;
    }

    // We need this to avoid going to long once different int values are passed,
    // due to the implicit cast int -> long.
    @Specialization(replaces = "cacheInt")
    protected int uncachedInt(int value) {
        return value;
    }

    @Specialization(guards = "value == cachedValue", limit = "1")
    protected long cacheLong(long value,
            @Cached("value") long cachedValue) {
        return cachedValue;
    }

    @Specialization(guards = "exactCompare(value, cachedValue)", limit = "1")
    protected double cacheDouble(double value,
            @Cached("value") double cachedValue) {
        return cachedValue;
    }

    @Specialization(guards = { "object != null", "object.getClass() == cachedClass", "!isPrimitiveClass(cachedClass)" }, limit = "1")
    protected Object cacheClass(Object object,
            @Cached("object.getClass()") Class<?> cachedClass) {
        // The cast is only useful for the compiler.
        if (CompilerDirectives.inInterpreter()) {
            return object;
        } else {
            return cachedClass.cast(object);
        }
    }

    @Specialization(replaces = { "cacheBoolean", "cacheInt", "uncachedInt", "cacheLong", "cacheDouble", "cacheClass" })
    protected Object unprofiled(Object object) {
        return object;
    }

    protected static boolean exactCompare(double a, double b) {
        // -0.0 == 0.0, but you can tell the difference through other means, so we need to differentiate.
        return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b);
    }

    @Override
    public String toString() {
        return "Profiled(" + getChild() + ")";
    }

}
