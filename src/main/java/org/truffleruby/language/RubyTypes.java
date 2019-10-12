/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
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

@TypeSystem
public abstract class RubyTypes {

    @ImplicitCast
    public static int promoteToInt(byte value) {
        return value;
    }

    @ImplicitCast
    public static int promoteToInt(short value) {
        return value;
    }

    @ImplicitCast
    public static long promoteToLong(byte value) {
        return value;
    }

    @ImplicitCast
    public static long promoteToLong(short value) {
        return value;
    }

    @ImplicitCast
    public static long promoteToLong(int value) {
        return value;
    }

    @ImplicitCast
    public static double promoteToDouble(float value) {
        return value;
    }

}
