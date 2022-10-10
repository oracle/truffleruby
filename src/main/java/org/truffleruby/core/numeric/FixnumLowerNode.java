/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyNode;

/** Passes through {@code int} values unmodified, but will convert a {@code long} value to an {@code int}, if it fits
 * within the range of an {@code int}. Leaves all other values unmodified. Used where a specialization only accepts
 * {@code int}, such as Java array indexing, but we would like to also handle {@code long} if they also fit within an
 * {@code int}.
 *
 * <p>
 * See {@link org.truffleruby.core.cast.ToIntNode} for a comparison of different integer conversion nodes. */
@NodeChild(value = "valueNode", type = RubyBaseNodeWithExecute.class)
public abstract class FixnumLowerNode extends RubyContextSourceNode {

    public static FixnumLowerNode create() {
        return FixnumLowerNodeGen.create(null);
    }

    public static FixnumLowerNode create(RubyBaseNodeWithExecute value) {
        return FixnumLowerNodeGen.create(value);
    }

    public abstract Object executeLower(Object value);

    abstract RubyBaseNodeWithExecute getValueNode();

    @Specialization
    protected int lower(int value) {
        return value;
    }

    @Specialization(guards = "fitsInInteger(value)")
    protected int lower(long value) {
        return (int) value;
    }

    @Specialization(guards = "!fitsInInteger(value)")
    protected long lowerFails(long value) {
        return value;
    }

    @Specialization(guards = "!isImplicitLong(value)")
    protected Object passThrough(Object value) {
        return value;
    }

    public RubyNode cloneUninitialized() {
        var copy = create(getValueNode().cloneUninitialized());
        return copy.copyFlags(this);
    }

}

