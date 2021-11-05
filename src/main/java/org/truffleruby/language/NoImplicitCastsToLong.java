/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
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

/** Same as {@link RubyTypes} but without implicit casts from * to long. */
@TypeSystem
public abstract class NoImplicitCastsToLong {

    // Ordered from most frequent to least frequent for interpreter performance

    // For handling interop primitives

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
