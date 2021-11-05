/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.dsl.ImplicitCast;
import com.oracle.truffle.api.dsl.TypeSystem;

/** Here are all types used for representing Ruby values (see {@link RubyGuards#isRubyValue(Object)}):
 * <ul>
 * <li>{@link RubyDynamicObject} subclasses</li>
 * <li>{@link ImmutableRubyObject} subclasses</li>
 * <li>Primitives: boolean, int, long, double are produced by Ruby nodes</li>
 * <li>Other primitives from foreign languages accepted in Ruby nodes via implicit cast: byte, short, float</li>
 * </ul>
 *
 * Also see {@link NoImplicitCastsToLong} */
@TypeSystem
public abstract class RubyTypes {

    // Ordered from most frequent to least frequent for interpreter performance

    @ImplicitCast
    public static long promoteToLong(int value) {
        return value;
    }

    // For handling interop primitives

    @ImplicitCast
    public static long promoteToLong(short value) {
        return value;
    }

    @ImplicitCast
    public static long promoteToLong(byte value) {
        return value;
    }

    @ImplicitCast
    public static int promoteToInt(short value) {
        return value;
    }

    @ImplicitCast
    public static int promoteToInt(byte value) {
        return value;
    }

    @ImplicitCast
    public static double promoteToDouble(float value) {
        return value;
    }

}
