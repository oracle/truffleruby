/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/**
 * Passes through {@code int} values unmodified, but will convert a {@code long} value to an {@code int}, if it fits
 * within the range of an {@code int}. Leaves all other values unmodified. Used where a specialization only accepts
 * {@code int}, such as Java array indexing, but we would like to also handle {@code long} if they also fit within an
 * {@code int}.
 */
@NodeChild(value = "value", type = RubyNode.class)
public abstract class FixnumLowerNode extends RubyNode {

    public static FixnumLowerNode create() {
        return FixnumLowerNodeGen.create(null);
    }

    public abstract Object executeLower(Object value);

    @Specialization
    public int lower(int value) {
        return value;
    }

    @Specialization(guards = "canLower(value)")
    public int lower(long value) {
        return (int) value;
    }

    @Specialization(guards = "!canLower(value)")
    public long lowerFails(long value) {
        return value;
    }

    @Specialization(guards = { "!isInteger(value)", "!isLong(value)" })
    public Object passThrough(Object value) {
        return value;
    }

    protected static boolean canLower(long value) {
        return CoreLibrary.fitsIntoInteger(value);
    }

}

